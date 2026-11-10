package org.example.entrada.client;


import org.example.entrada.client.pago.response.dto.PagoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "pago-service", url = "http://localhost:8082/api/pagos")
public interface PagoFeignClient {
    @PostMapping("/reserva/{idReserva}/reembolso")
    ResponseEntity<PagoResponseDTO> reembolsarPago(@PathVariable Long idReserva);
}
