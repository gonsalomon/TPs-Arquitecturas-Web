package org.example.evento.controller;

import lombok.RequiredArgsConstructor;
import org.example.evento.dto.response.EventoRecomendadoDTO;
import org.example.evento.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    // "Quienes compraron entradas para este evento también compraron para..."
    @GetMapping("/{eventoId}/similares")
    public ResponseEntity<List<EventoRecomendadoDTO>> obtenerSimilares(
            @PathVariable String eventoId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(recomendacionService.obtenerEventosSimilares(eventoId, limit));
    }

    // Recomendaciones personalizadas por historial + preferencias del cliente
    @GetMapping("/recomendaciones")
    public ResponseEntity<List<EventoRecomendadoDTO>> obtenerRecomendaciones(
            @RequestHeader("X-Usuario-Id") Long clienteId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recomendacionService.obtenerRecomendacionesParaCliente(clienteId, limit));
    }
}