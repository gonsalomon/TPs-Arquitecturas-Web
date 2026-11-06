package org.example.orden.feignClients;

import org.example.orden.dto.response.EventoExternoDTO;
import org.example.orden.dto.response.LugarExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "evento-client", url = "${servicios.evento.url}")
public interface EventoClient {

    @GetMapping("/eventos/{id}")
    EventoExternoDTO obtenerEvento(@PathVariable("id") String id);

    @GetMapping("/lugares/{id}")
    LugarExternoDTO obtenerLugar(@PathVariable("id") String id);
}
