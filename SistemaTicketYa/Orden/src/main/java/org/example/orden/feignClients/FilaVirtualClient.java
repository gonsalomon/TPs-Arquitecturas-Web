package org.example.orden.feignClients;

import org.example.orden.dto.response.ValidacionFilaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "fila-client", url = "http://localhost:8088")
public interface FilaVirtualClient {

    @GetMapping("/api/fila/validar")
    ValidacionFilaDTO validarAcceso(
            @RequestParam("clienteId") Long clienteId,
            @RequestParam("eventoId") String eventoId,
            @RequestParam(value = "tokenAcceso", required = false) String tokenFila);

    @PostMapping("/api/fila/eventos/{eventoId}/completar")
    void completarTurno(
            @RequestHeader("X-Usuario-Id") Long clienteId,
            @PathVariable("eventoId") String eventoId);
}
