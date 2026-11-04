package org.example.entrada.controller;

import jakarta.validation.Valid;
import org.example.entrada.dto.CambioEstadoRequest;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.service.EntradaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    // Orden -> Entrada, after Pago approved the payment. Idempotent by ordenId. Internal: do not expose through the Gateway.
    @PostMapping
    public ResponseEntity<List<EntradaDTO>> emitir(@Valid @RequestBody EmitirEntradasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitir(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // GET /entradas?clienteId=1&eventoId=6720ab... (both optional, at least one required)
    @GetMapping
    public ResponseEntity<List<EntradaDTO>> buscar(@RequestParam(required = false) Long clienteId,
                                                   @RequestParam(required = false) String eventoId) {
        return ResponseEntity.ok(service.buscar(clienteId, eventoId));
    }

    // Orden/Reserva: check the per-client limit BEFORE charging
    @GetMapping("/cliente/{clienteId}/evento/{eventoId}/cantidad")
    public ResponseEntity<Map<String, Long>> cantidad(@PathVariable Long clienteId, @PathVariable String eventoId) {
        return ResponseEntity.ok(Map.of("cantidad", service.contarActivas(clienteId, eventoId)));
    }

    // Orden -> Entrada: switch one ticket's state (e.g. refund approved: REINTEGRO + motivo DEVOLUCION)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntradaDTO> cambiarEstado(@PathVariable Long id,
                                                    @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request));
    }

    // Orden -> Entrada when an event is cancelled. Returns the affected tickets (to refund and notify).
    @PatchMapping("/evento/{eventoId}/reintegro")
    public ResponseEntity<List<EntradaDTO>> reintegroPorEvento(@PathVariable String eventoId) {
        return ResponseEntity.ok(service.reintegroPorEvento(eventoId));
    }

    // Orden -> Entrada as a compensation when a later step of a purchase fails
    @PatchMapping("/orden/{ordenId}/reintegro")
    public ResponseEntity<List<EntradaDTO>> reintegroPorOrden(@PathVariable Long ordenId) {
        return ResponseEntity.ok(service.reintegroPorOrden(ordenId));
    }
}