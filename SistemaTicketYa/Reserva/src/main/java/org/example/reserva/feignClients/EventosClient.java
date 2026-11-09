package org.example.reserva.feignClients;
import org.example.reserva.dto.request.EventoExternoDTO;
import org.example.reserva.dto.request.LugarExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "evento-client", url = "http://localhost:8083/api", primary = false)
public interface EventosClient {
    @GetMapping("/eventos/{id}")
    EventoExternoDTO obtenerEvento(@PathVariable("id") String id);

    @GetMapping("/lugares/{id}")
    LugarExternoDTO obtenerLugar(@PathVariable("id") String id);

}