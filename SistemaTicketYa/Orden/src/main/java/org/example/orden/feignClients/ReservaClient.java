package org.example.orden.feignClients;

import org.example.orden.dto.request.ReservaCrearRequestDTO;
import org.example.orden.dto.response.ReservaExternaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "reserva-client", url = "${servicios.reserva.url}")
public interface ReservaClient {

    @PostMapping("/reservas")
    ReservaExternaDTO crearReserva(@RequestBody ReservaCrearRequestDTO request);

    @PutMapping("/reservas/{id}/estado")
    void actualizarEstado(@PathVariable("id") Long id, @RequestParam("nuevoEstado") String nuevoEstado);
}
