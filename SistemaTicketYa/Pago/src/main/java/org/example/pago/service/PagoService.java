package org.example.pago.service;

import lombok.AllArgsConstructor;
import org.example.pago.dto.request.PagoRequestDTO;
import org.example.pago.dto.request.ReservaExternatDTO;
import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.entity.Pago;
import org.example.pago.feignClients.ReservaClient;
import org.example.pago.mapper.PagoMapper;
import org.example.pago.repository.PagoRepository;
import org.example.pago.utils.Estado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PagoService {
    private final PagoMapper mapper;
    private final ReservaClient reservaClient;
    private final PagoRepository repository;
    private static final int LIMITE_ENTRADAS_POR_CLIENTE = 5;

    @Transactional
    public PagoResponseDTO realizarPago(PagoRequestDTO request) {

        if (repository.existsByIdReserva(request.getIdReserva())) {
            throw new IllegalStateException("Esta reserva ya se encuentra pagada.");
        }

        // 1. FetchClient: Buscamos la reserva en el microservicio
        ReservaExternatDTO reserva = reservaClient.obtenerReservaPorId(request.getIdReserva());

        // 2. Validaciones locales (cliente, tiempos, estados)
        if (!reserva.getIdCliente().equals(request.getIdCliente())) {
            throw new IllegalArgumentException("El cliente no coincide.");
        }

        if (LocalDateTime.now().isAfter(reserva.getFechaVencimiento())) {
            reservaClient.actualizarEstadoReserva(reserva.getIdReserva(), "VENCIDA");
            throw new IllegalStateException("Reserva expirada.");
        }

        // 3. FetchClient: Validamos el límite de entradas consultando al otro microservicio
        List<ReservaExternatDTO> confirmadas = reservaClient.obtenerReservasConfirmadas(
                request.getIdCliente(),
                reserva.getIdEvento()
        );

        int entradasYaCompradas = confirmadas.stream()
                .filter(r -> r.getAsientos() != null)
                .mapToInt(r -> r.getAsientos().size())
                .sum();

        if ((entradasYaCompradas + reserva.getAsientos().size()) > LIMITE_ENTRADAS_POR_CLIENTE) {
            reservaClient.actualizarEstadoReserva(reserva.getIdReserva(), "VENCIDA");
            throw new IllegalStateException("Límite de entradas excedido.");
        }

        // 4. Guardamos el pago de forma local
        Pago nuevoPago = mapper.convertToEntity(request);
        nuevoPago.setIdReserva(reserva.getIdReserva()); // Guardamos solo el ID
        nuevoPago.setEstado(Estado.ACEPTADO);
        nuevoPago.setFecha_emision(LocalDateTime.now());
        nuevoPago.setMonto(reserva.getMontoTotal());

        Pago pagoGuardado = repository.save(nuevoPago);

        // 5. FetchClient: Confirmamos la reserva en el otro microservicio
        reservaClient.actualizarEstadoReserva(reserva.getIdReserva(), "CONFIRMADA");

        return mapper.convertToDTO(pagoGuardado);
    }

    @Transactional
    public List<PagoResponseDTO> obtenerTodosLosPagos() {
        return repository.findAll().stream()
                .map(mapper::convertToDTO)
                .collect(Collectors.toList());
    }

}
