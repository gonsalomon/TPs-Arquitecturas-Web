package org.example.orden.feignClients;

import org.example.orden.dto.request.PagoCrearRequestDTO;
import org.example.orden.dto.response.PagoExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pago-client", url = "http://localhost:8082/api/pagos")
public interface PagoClient {

    @PostMapping("")
    PagoExternoDTO realizarPago(@RequestBody PagoCrearRequestDTO request);
    @PostMapping("/reserva/{idReserva}/reembolso")
    PagoExternoDTO reembolsarPago(@PathVariable Long idReserva);
    @GetMapping("/{id}")
    PagoExternoDTO obtenerPagoPorId(@PathVariable Long id);
}
