package org.example.entrada.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reserva-service", url = "http://localhost:8084/api/reservas")
public interface ReservaFeignClient {
    @PutMapping("/{id}/estado")
    ResponseEntity<Void> actualizarEstadoReserva(@PathVariable Long id, @RequestParam String nuevoEstado);
}
