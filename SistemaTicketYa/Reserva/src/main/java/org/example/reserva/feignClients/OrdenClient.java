package org.example.reserva.feignClients;

import org.example.reserva.dto.request.OrdenExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "orden-service", url = "http://localhost:8087/api/ordenes")
public interface OrdenClient {

    @GetMapping("/{id}")
    OrdenExternoDTO obtenerOrden(@PathVariable("id") Long id);
}
