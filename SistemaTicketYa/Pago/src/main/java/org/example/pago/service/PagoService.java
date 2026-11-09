package org.example.pago.service;

import lombok.AllArgsConstructor;

import org.example.pago.dto.request.PagoRequestDTO;
import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.entity.Pago;
import org.example.pago.feignClients.OrdenClient;
import org.example.pago.mapper.PagoMapper;
import org.example.pago.repository.PagoRepository;
import org.example.pago.utils.Estado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PagoService {
    private final PagoMapper mapper;
    private final OrdenClient ordenClient;
    private final PagoRepository repository;

    @Transactional
    public PagoResponseDTO realizarPago(PagoRequestDTO request) {

        // 1. Evita doble cobro por seguridad
        if (repository.existsByIdReserva(request.getIdReserva())) {
            throw new IllegalStateException("Esta reserva ya tiene un pago registrado.");
        }

        // 2. Crea la entidad de pago
        Pago nuevoPago = mapper.convertToEntity(request);
        nuevoPago.setFecha_emision(LocalDateTime.now());

        // lógica simple de aprobación
        if (request.getMonto() != null && request.getMonto().compareTo(BigDecimal.ZERO) > 0) {
            nuevoPago.setEstado(Estado.ACEPTADO);
        } else {
            nuevoPago.setEstado(Estado.RECHAZADO);
        }

        // 3. Guarda en base de datos local de Pagos
        Pago pagoGuardado = repository.save(nuevoPago);

        return mapper.convertToDTO(pagoGuardado);
    }

    @Transactional
    public List<PagoResponseDTO> obtenerTodosLosPagos() {
        return repository.findAll().stream()
                .map(mapper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoResponseDTO procesarReembolso(Long idReserva) {
        // 1. Buscamos el pago original que esté APROBADO
        Pago pago = repository.findByIdReservaAndEstado(idReserva, Estado.ACEPTADO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un pago aprobado para la reserva ID: " + idReserva));

        // 3. Actualizamos el estado local
        pago.setEstado(Estado.REEMBOLSADO);

        pago.setFecha_reembolso(LocalDateTime.now());

        // 3. Guardamos los cambios
        Pago pagoActualizado = repository.save(pago);

        return mapper.convertToDTO(pagoActualizado);
    }

}
