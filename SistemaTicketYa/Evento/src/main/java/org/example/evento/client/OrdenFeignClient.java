package org.example.evento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "orden-client", url = "http://localhost:8087/api/ordenes")
public interface OrdenFeignClient {
    @PutMapping("/{idEvento}/cancelacion")
    void cancelacionEvento(@PathVariable String idEvento);

    @PutMapping("/evento/{idEvento}/cambio-fecha")
    void notificarCambioFecha(
            @PathVariable String idEvento,
            @RequestParam LocalDateTime fechaAnterior,
            @RequestParam LocalDateTime fechaNueva
    );
}
