package org.example.pago.feignClients;
import org.example.pago.dto.request.ReservaExternatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Apunta a la URL donde correrá el microservicio de reservas (ej. puerto 8081)
@FeignClient(name = "reserva-client", url = "http://localhost:8084/api/reservas")
public interface ReservaClient {

    // Simula un GET a http://localhost:8081/api/reservas/{id}
    @GetMapping("/{id}")
    ReservaExternatDTO obtenerReservaPorId(@PathVariable("id") Long id);

    // Simula un PUT o PATCH para actualizar el estado de la reserva tras pagar
    @PutMapping("/{id}/estado")
    void actualizarEstadoReserva(@PathVariable("id") Long id, @RequestParam("nuevoEstado") String estado);

    // Simula un GET para traer las reservas confirmadas del cliente (para la validación del límite)
    @GetMapping("/cliente/{idCliente}/evento/{idEvento}/confirmadas")
    List<ReservaExternatDTO> obtenerReservasConfirmadas(
            @PathVariable("idCliente") Long idCliente,
            @PathVariable("idEvento") String idEvento
    );
}
