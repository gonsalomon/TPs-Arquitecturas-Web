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
        // 1. VALIDACIÓN EXTERNA: Verificar que el usuario exista
        // Si el usuario no existe, Feign arrojará una excepción 404 (FeignException.NotFound)
        UsuarioExternoDTO usuario = usuarioClient.obtenerUsuario(request.getIdCliente());

        // 2. VALIDACIÓN EXTERNA: Verificar que el evento exista
        EventoExternoDTO evento = eventosClient.obtenerEvento(request.getIdEvento());

        // 3. VALIDACIÓN EXTERNA: Obtener el lugar, verificar el sector y sus asientos
        LugarExternoDTO lugar = eventosClient.obtenerLugar(evento.getIdLugar());

        SectorExternoDTO sectorSolicitado = lugar.getSectores().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase(request.getSector()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El sector solicitado no existe en este lugar."));

        // Validar que los asientos pedidos realmente pertenezcan a este sector físicamente
        boolean asientosValidos = sectorSolicitado.getAsientosPermitidos().containsAll(request.getAsientos());
        if (!asientosValidos) {
            throw new IllegalArgumentException("Uno o más asientos seleccionados no pertenecen al sector " + request.getSector());
        }

        // 4. CÁLCULO DE PRECIO: (Cantidad de asientos * Precio del sector)
        Float precioSector = sectorSolicitado.getPrecio();
        Float precioTotal = precioSector * request.getAsientos().size();

        // 5. VALIDACIÓN INTERNA: Chequear que los asientos no estén ocupados por otra reserva
        List<String> estadosActivos = List.of(
                EstadoReserva.PENDIENTE.name(),
                EstadoReserva.CONFIRMADA.name()
        );
        long asientosOcupados = repository.contarAsientosOcupados(request.getIdEvento(), estadosActivos, request.getAsientos());

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
}
