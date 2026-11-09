package org.example.reserva.service;

import lombok.AllArgsConstructor;

import org.example.reserva.dto.request.*;
import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.entity.Reserva;
import org.example.reserva.feignClients.EventosClient;
import org.example.reserva.feignClients.UsuarioClient;
import org.example.reserva.mapper.ReservaMapper;
import org.example.reserva.repository.ReservaRepository;
import org.example.reserva.utils.EstadoReserva;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservaService {
    private final ReservaMapper mapper;
    private final ReservaRepository repository;
    private final UsuarioClient usuarioClient;
    private final EventosClient eventosClient;

    private static final int LIMITE_ENTRADAS_POR_CLIENTE = 20;

    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request) {
        // 1 y 2. VALIDACIÓN EXTERNA (Usuario y Evento)
        UsuarioExternoDTO usuario = usuarioClient.obtenerUsuario(request.getIdCliente());
        EventoExternoDTO evento = eventosClient.obtenerEvento(request.getIdEvento());

        // 3. VALIDACIÓN EXTERNA (Lugar y Sector)
        LugarExternoDTO lugar = eventosClient.obtenerLugar(evento.getIdLugar());

        SectorExternoDTO sectorSolicitado = lugar.getSectores().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(request.getSector()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El sector solicitado no existe."));

        // Sector no numerado (zona): Orden no manda asientos
        List<Long> asientos = request.getAsientos() == null ? List.of() : request.getAsientos();
        request.setAsientos(asientos);

        // Numerado: los asientos van de 1 a filas x asientosPorFila (capacidad solo existe en las zonas)
        Integer maximo = (sectorSolicitado.getFilas() != null && sectorSolicitado.getAsientosPorFila() != null)
                ? sectorSolicitado.getFilas() * sectorSolicitado.getAsientosPorFila()
                : sectorSolicitado.getCapacidad();
        boolean asientosValidos = asientos.stream()
                .allMatch(asiento -> asiento > 0 && maximo != null && asiento <= maximo);

        if (!asientosValidos) {
            throw new IllegalArgumentException("Uno o más asientos seleccionados no son válidos o exceden la capacidad para este sector.");
        }


        Float precioTotal = evento.getPrecio() * asientos.size();

        // 5. VALIDACIÓN INTERNA (Asientos ocupados)
        List<EstadoReserva> estadosActivos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

        // "IN ()" vacío no es SQL válido: en una zona no hay asientos que chequear
        long asientosOcupados = asientos.isEmpty()
                ? 0
                : repository.contarAsientosOcupados(request.getIdEvento(), estadosActivos, asientos);

        if (asientosOcupados > 0) {
            throw new IllegalStateException("Los asientos solicitados ya están ocupados.");
        }

        // 6. PERSISTENCIA
        Reserva nuevaReserva = mapper.convertToEntity(request);
        nuevaReserva.setMonto(precioTotal);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
        nuevaReserva.setFecha_inicio(LocalDateTime.now());
        nuevaReserva.setFechaVencimiento(LocalDateTime.now().plusMinutes(10));

        Reserva reservaGuardada = repository.save(nuevaReserva);

        return mapper.convertToDTO(reservaGuardada);
    }

    @Transactional
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
        // Usamos el estado CONFIRMADA convertido a String, tal como ajustaste antes
        List<String> estadosConfirmados = List.of("CONFIRMADA"); // O EstadoReserva.CONFIRMADA.name()

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

        reserva.setEstado(EstadoReserva.valueOf(nuevoEstado.toUpperCase()));

        repository.save(reserva);
    }

    @Scheduled(fixedRate = 60000) //Verifica cada 1 minuto
    @Transactional
    public void liberarReservasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscamos todas las reservas PENDIENTES que ya pasaron sus 10 minutos
        List<Reserva> reservasExpiradas = repository.findByEstadoAndFechaVencimientoBefore(
                EstadoReserva.PENDIENTE, ahora
        );

        if (!reservasExpiradas.isEmpty()) {
            for (Reserva reserva : reservasExpiradas) {
                reserva.setEstado(EstadoReserva.VENCIDA); // Cambiamos el estado
            }
            // Guardamos los cambios masivamente
            repository.saveAll(reservasExpiradas);
            System.out.println("Se liberaron " + reservasExpiradas.size() + " reservas vencidas.");
        }
    }

    public byte[] generarQRReserva(Long idReserva) {
        // 1. Validar que la reserva exista y esté pagada
        Reserva reserva = repository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!"CONFIRMADA".equalsIgnoreCase(reserva.getEstado().name())) {
            throw new IllegalStateException("Solo se pueden generar QRs para reservas confirmadas y pagadas.");
        }

        // 2. Armar el contenido del QR (ejemplo: JSON o cadena de validación)
        String contenidoQr = String.format("TicketYa | Reserva: %d | Cliente: %s | Evento: %d | Asientos: %s",
                reserva.getIdReserva(), reserva.getIdCliente(), reserva.getIdEvento(), reserva.getAsientos());

        // 3. Generar la imagen usando ZXing
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
