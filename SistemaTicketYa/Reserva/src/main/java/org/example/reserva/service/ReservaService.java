package org.example.reserva.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.example.reserva.dto.request.*;
import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.entity.AsientoBloqueado;
import org.example.reserva.entity.Reserva;
import org.example.reserva.feignClients.EventosClient;
import org.example.reserva.feignClients.OrdenClient;
import org.example.reserva.feignClients.UsuarioClient;
import org.example.reserva.mapper.ReservaMapper;
import org.example.reserva.repository.AsientoBloqueadoRepository;
import org.example.reserva.repository.BloqueoSectorRepository;
import org.example.reserva.repository.ReservaRepository;
import org.example.reserva.utils.EstadoReserva;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservaService {

    private final ReservaMapper mapper;
    private final ReservaRepository repository;
    private final EventosClient eventosClient;
    private final OrdenClient ordenClient;
    private final UsuarioClient usuarioClient;
    private final AsientoBloqueadoRepository asientoBloqueadoRepository;
    private final BloqueoSectorRepository bloqueoSectorRepository;
    private final BloqueoSectorHelper bloqueoSectorHelper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request) {
        return crearReserva(request, null, null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request, Long usuarioIdHeader) {
        return crearReserva(request, usuarioIdHeader, null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request, Long usuarioIdHeader, String idempotencyKey) {
        // A. IDEMPOTENCIA (si la clave ya fue procesada, devuelve la reserva original)
        String claveIdempotencia = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey.trim()
                : null;

        if (claveIdempotencia != null) {
            Optional<Reserva> existente = repository.findByIdempotencyKey(claveIdempotencia);
            if (existente.isPresent()) {
                return mapper.convertToDTO(existente.get());
            }
        }

        // 0. VALIDACIÓN DE CLIENTE CONTRA USUARIO AUTENTICADO Y ORDEN
        if (request.getIdCliente() == null || request.getIdCliente() <= 0) {
            throw new IllegalArgumentException("El idCliente es inválido.");
        }

        try {
            ResponseEntity<Object> respuestaUsuario = usuarioClient.obtenerUsuarioPorId(request.getIdCliente());
            if (respuestaUsuario == null || !respuestaUsuario.getStatusCode().is2xxSuccessful() || respuestaUsuario.getBody() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el cliente con ID: " + request.getIdCliente()
                );
            }
        } catch (FeignException.NotFound | FeignException.BadRequest e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe el cliente con ID: " + request.getIdCliente()
            );
        }

        if (usuarioIdHeader != null && !Objects.equals(request.getIdCliente(), usuarioIdHeader)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El cliente de la solicitud no coincide con el usuario autenticado."
            );
        }

        Long idClienteEfectivo = usuarioIdHeader != null ? usuarioIdHeader : request.getIdCliente();

        if (request.getIdOrden() != null) {
            OrdenExternoDTO orden;
            try {
                orden = ordenClient.obtenerOrden(request.getIdOrden());
            } catch (FeignException.NotFound e) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la orden con ID: " + request.getIdOrden()
                );
            }

            if (orden == null || !Objects.equals(orden.getClienteId(), idClienteEfectivo)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El cliente no corresponde al titular de la orden " + request.getIdOrden()
                );
            }
        }

        // 1 y 2. VALIDACIÓN EXTERNA (Evento y Lugar)
        EventoExternoDTO evento = eventosClient.obtenerEvento(request.getIdEvento());
        LugarExternoDTO lugar = eventosClient.obtenerLugar(evento.getIdLugar());

        SectorExternoDTO sectorSolicitado = lugar.getSectores().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(request.getSector()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El sector solicitado no existe."));

        String sectorNorm = sectorSolicitado.getNombre().trim().toUpperCase(Locale.ROOT);

        // 3. PREPARACIÓN DE VARIABLES SEGURAS
        List<EstadoReserva> estadosActivos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        List<Long> asientosPedidos = request.getAsientos() != null ? request.getAsientos() : new ArrayList<>();

        int cantidadPedida = request.getCantidad() != null ? request.getCantidad() : 0;
        int cantidadEntradas = asientosPedidos.isEmpty() ? cantidadPedida : asientosPedidos.size();

        if (cantidadEntradas <= 0) {
            throw new IllegalArgumentException("Debe solicitar al menos una entrada (cantidad mayor a 0 o enviar asientos).");
        }

        boolean esNumerado = "NUMERADO".equalsIgnoreCase(sectorSolicitado.getTipo());

        // 4. VALIDACIÓN DIVIDIDA POR TIPO DE SECTOR
        if (esNumerado) {
            if (asientosPedidos.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un asiento para un sector numerado.");
            }
            if (new HashSet<>(asientosPedidos).size() != asientosPedidos.size()) {
                throw new IllegalArgumentException("La solicitud contiene asientos duplicados.");
            }

            long maxAsientoFisico = (sectorSolicitado.getFilas() != null && sectorSolicitado.getAsientosPorFila() != null)
                    ? sectorSolicitado.getFilas() * sectorSolicitado.getAsientosPorFila()
                    : sectorSolicitado.getCapacidad();

            boolean asientosValidos = asientosPedidos.stream()
                    .allMatch(asiento -> asiento != null && asiento > 0 && asiento <= maxAsientoFisico);

            if (!asientosValidos) {
                throw new IllegalArgumentException("Uno o más asientos seleccionados no existen en este sector.");
            }

            // Validación rápida previa (la garantía real contra concurrencia la da el UNIQUE en asiento_bloqueado)
            long asientosOcupados = repository.contarAsientosOcupados(
                    request.getIdEvento(),
                    sectorSolicitado.getNombre(),
                    estadosActivos,
                    asientosPedidos
            );
            if (asientosOcupados > 0) {
                throw new IllegalStateException("Los asientos solicitados ya están ocupados.");
            }

        } else {
            // LOCK PESIMISTA EN BD (SELECT ... FOR UPDATE) PARA EVITAR SOBREVENTA MULTI-INSTANCIA
            if (bloqueoSectorRepository.findWithLock(request.getIdEvento(), sectorNorm).isEmpty()) {
                bloqueoSectorHelper.asegurarFilaExiste(request.getIdEvento(), sectorNorm);
                bloqueoSectorRepository.findWithLock(request.getIdEvento(), sectorNorm)
                        .orElseThrow(() -> new IllegalStateException("No se pudo adquirir bloqueo de sector."));
            }

            long entradasYaVendidas = repository.contarEntradasVendidasPorSector(
                    request.getIdEvento(),
                    sectorSolicitado.getNombre(),
                    estadosActivos
            );

            if ((entradasYaVendidas + cantidadEntradas) > sectorSolicitado.getCapacidad()) {
                throw new IllegalStateException("No hay capacidad suficiente en este sector. Disponibles: " +
                        (sectorSolicitado.getCapacidad() - entradasYaVendidas));
            }
        }

        // 5. CÁLCULO DE PRECIO
        Float precioTotal = evento.getPrecio() * cantidadEntradas;

        // 6. PERSISTENCIA
        Reserva nuevaReserva = mapper.convertToEntity(request);

        nuevaReserva.setSector(sectorSolicitado.getNombre());
        nuevaReserva.setAsientos(asientosPedidos);
        nuevaReserva.setCantidad(cantidadEntradas);
        nuevaReserva.setMonto(precioTotal);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
        nuevaReserva.setFecha_inicio(LocalDateTime.now());
        nuevaReserva.setFechaVencimiento(LocalDateTime.now().plusMinutes(10));
        nuevaReserva.setIdempotencyKey(claveIdempotencia);

        Reserva reservaGuardada = repository.saveAndFlush(nuevaReserva);

        // BLOQUEO ATÓMICO EN BD PARA ASIENTOS NUMERADOS (ordenados para prevenir deadlocks)
        if (esNumerado) {
            List<AsientoBloqueado> bloqueos = asientosPedidos.stream()
                    .sorted()
                    .map(asiento -> new AsientoBloqueado(
                            null,
                            request.getIdEvento(),
                            sectorNorm,
                            asiento,
                            reservaGuardada.getIdReserva()
                    ))
                    .toList();
            asientoBloqueadoRepository.saveAllAndFlush(bloqueos);
        }

        return mapper.convertToDTO(reservaGuardada);
    }

    @Transactional(readOnly = true)
    public Optional<ReservaResponseDTO> buscarPorIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey.trim())
                .map(mapper::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> obtenerTodasLasReservas() {
        return repository.findAll().stream()
                .map(mapper::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReservaResponseDTO obtenerReservaPorId(Long idReserva) {
        Reserva reserva = repository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con el ID: " + idReserva));

        return mapper.convertToDTO(reserva);
    }

    public List<ReservaResponseDTO> obtenerReservasConfirmadas(Long idCliente, String idEvento) {
        List<String> estadosConfirmados = List.of(EstadoReserva.CONFIRMADA.name());

        List<Reserva> reservas = repository.buscarReservasPorCliente(
                idCliente,
                idEvento,
                estadosConfirmados
        );

        return reservas.stream()
                .map(mapper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void actualizarEstado(Long idReserva, String nuevoEstado) {
        Reserva reserva = repository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + idReserva));

        EstadoReserva estadoNuevo = EstadoReserva.valueOf(nuevoEstado.toUpperCase(Locale.ROOT));
        reserva.setEstado(estadoNuevo);
        repository.save(reserva);

        // Liberar los asientos bloqueados si la reserva deja de estar activa (ej. VENCIDA, CANCELADA, REEMBOLSADA)
        if (estadoNuevo != EstadoReserva.PENDIENTE && estadoNuevo != EstadoReserva.CONFIRMADA) {
            asientoBloqueadoRepository.deleteByIdReserva(idReserva);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarReservasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Reserva> reservasExpiradas = repository.findByEstadoAndFechaVencimientoBefore(
                EstadoReserva.PENDIENTE, ahora
        );

        if (!reservasExpiradas.isEmpty()) {
            List<Long> idsExpiradas = new ArrayList<>();
            for (Reserva reserva : reservasExpiradas) {
                reserva.setEstado(EstadoReserva.VENCIDA);
                idsExpiradas.add(reserva.getIdReserva());
            }
            repository.saveAll(reservasExpiradas);
            asientoBloqueadoRepository.deleteByIdReservaIn(idsExpiradas);
            System.out.println("Se liberaron " + reservasExpiradas.size() + " reservas vencidas.");
        }
    }

    public byte[] generarQRReserva(Long idReserva) {
        Reserva reserva = repository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!"CONFIRMADA".equalsIgnoreCase(reserva.getEstado().name())) {
            throw new IllegalStateException("Solo se pueden generar QRs para reservas confirmadas y pagadas.");
        }

        String contenidoQr = String.format("TicketYa | Reserva: %d | Cliente: %s | Evento: %s | Asientos: %s",
                reserva.getIdReserva(), reserva.getIdCliente(), reserva.getIdEvento(), reserva.getAsientos());

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenidoQr, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el código QR", e);
        }
    }
}
