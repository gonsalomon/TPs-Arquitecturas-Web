package org.example.pago.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "orden-client", url = "http://localhost:8087/api/ordenes")
public interface OrdenClient {

    // Avisamos a la Orden si el pago fue exitoso o falló
    @PutMapping("/reserva/{idReserva}/pago")
    void notificarResultadoPago(@PathVariable("idReserva") Long idReserva, @RequestParam("estado") String estado);
}
