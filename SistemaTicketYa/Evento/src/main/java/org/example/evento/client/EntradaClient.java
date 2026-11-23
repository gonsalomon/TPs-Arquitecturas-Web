package org.example.evento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "entrada-service", url = "${ENTRADA_URL:http://localhost:8085}/api/entradas")
public interface EntradaClient {

    @GetMapping("/api/entradas/co-compras/evento/{eventoId}")
    List<String> obtenerCoCompradosPorEvento(
            @PathVariable("eventoId") String eventoId,
            @RequestParam("limit") int limit
    );

    @GetMapping("/api/entradas/co-compras/cliente/{clienteId}")
    List<String> obtenerRecomendadosPorHistorial(
            @PathVariable("clienteId") Long clienteId,
            @RequestParam("limit") int limit
    );

    @GetMapping("/api/entradas/cliente/{clienteId}/eventos")
    List<String> obtenerEventosComprados(@PathVariable("clienteId") Long clienteId);
}
