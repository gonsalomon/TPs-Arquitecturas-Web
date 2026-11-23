package org.example.fila_virtual.feignClient;

import org.example.fila_virtual.DTO.Request.EventoExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "evento-client", url = "${EVENTO_URL:http://localhost:8083}/api")
public interface EventoClient {

    @GetMapping("/eventos/{id}")
    EventoExternoDTO obtenerEvento(@PathVariable("id") String id);
}
