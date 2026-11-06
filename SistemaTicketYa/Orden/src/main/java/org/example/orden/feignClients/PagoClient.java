package org.example.orden.feignClients;

import org.example.orden.dto.request.PagoCrearRequestDTO;
import org.example.orden.dto.response.PagoExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pago-client", url = "${servicios.pago.url}")
public interface PagoClient {

    @PostMapping("/pagos")
    PagoExternoDTO realizarPago(@RequestBody PagoCrearRequestDTO request);
}
