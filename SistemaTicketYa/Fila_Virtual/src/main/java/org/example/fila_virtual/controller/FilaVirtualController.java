package org.example.fila_virtual.controller;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.example.fila_virtual.DTO.Request.IngresarFilaRequestDTO;
import org.example.fila_virtual.DTO.Request.ValidacionAccesoDTO;
import org.example.fila_virtual.DTO.Response.TurnoResponseDTO;
import org.example.fila_virtual.service.FilaVirtualService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fila")
@RequiredArgsConstructor
public class FilaVirtualController {

    private static final String HEADER_CLIENTE = "X-Usuario-Id";
    private final FilaVirtualService service;

    @PostMapping("/ingresar")
    public ResponseEntity<TurnoResponseDTO> ingresar(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @Valid @RequestBody IngresarFilaRequestDTO request) {
        return ResponseEntity.ok(service.ingresar(clienteId, request.getEventoId()));
    }

    @GetMapping("/eventos/{eventoId}/estado")
    public ResponseEntity<TurnoResponseDTO> consultarEstado(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @PathVariable String eventoId) {
        return ResponseEntity.ok(service.consultarPosicion(clienteId, eventoId));
    }

    // Endpoint interno que consumirá el microservicio de Orden antes de permitir crear una orden
    @GetMapping("/validar")
    public ResponseEntity<ValidacionAccesoDTO> validarAcceso(
            @RequestParam Long clienteId,
            @RequestParam String eventoId,
            @RequestParam(required = false) String tokenAcceso) {
        return ResponseEntity.ok(service.validarAcceso(clienteId, eventoId, tokenAcceso));
    }

    // Endpoint que llamará Orden cuando el cliente termine la compra (libera el cupo al instante)
    @PostMapping("/eventos/{eventoId}/completar")
    public ResponseEntity completarTurno(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @PathVariable String eventoId) {
        service.marcarCompletado(clienteId, eventoId);
        return ResponseEntity.ok(Map.of("mensaje", "Turno completado. Se liberó el cupo para la siguiente tanda."));
    }

    @DeleteMapping("/eventos/{eventoId}/abandonar")
    public ResponseEntity abandonar(
            @RequestHeader(HEADER_CLIENTE) Long clienteId,
            @PathVariable String eventoId) {
        service.abandonarFila(clienteId, eventoId);
        return ResponseEntity.ok(Map.of("mensaje", "Saliste de la fila virtual."));
    }

// Útil para forzar el avance de tanda manualmente en pruebas o desde un panel admin
    @PostMapping("/eventos/{eventoId}/avanzar-tanda")
    public ResponseEntity avanzarTandaManual(@PathVariable String eventoId) {
        int promovidos = service.avanzarTandaEvento(eventoId);
        return ResponseEntity.ok(Map.of("eventoId", eventoId, "nuevosHabilitados", promovidos));
    }
}
