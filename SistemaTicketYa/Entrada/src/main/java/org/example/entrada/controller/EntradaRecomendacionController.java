package org.example.entrada.controller;

import lombok.RequiredArgsConstructor;
import org.example.entrada.service.EntradaRecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entradas")
@RequiredArgsConstructor
public class EntradaRecomendacionController {

    private final EntradaRecomendacionService recomendacionService;

    @GetMapping("/co-compras/evento/{eventoId}")
    public ResponseEntity<List<String>> obtenerCoCompradosPorEvento(
            @PathVariable String eventoId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(recomendacionService.obtenerCoCompradosPorEvento(eventoId, limit));
    }

    @GetMapping("/co-compras/cliente/{clienteId}")
    public ResponseEntity<List<String>> obtenerRecomendadosPorHistorial(
            @PathVariable Long clienteId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recomendacionService.obtenerRecomendadosPorHistorial(clienteId, limit));
    }

    @GetMapping("/cliente/{clienteId}/eventos")
    public ResponseEntity<List<String>> obtenerEventosComprados(@PathVariable Long clienteId) {
        return ResponseEntity.ok(recomendacionService.obtenerEventosComprados(clienteId));
    }
}
