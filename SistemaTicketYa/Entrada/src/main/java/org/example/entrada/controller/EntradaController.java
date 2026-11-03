package org.example.entrada.controller;

import jakarta.validation.Valid;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.service.EntradaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    // Called by Orden/Pago once the payment is approved. Idempotent by ordenId.
    @PostMapping
    public ResponseEntity<List<EntradaDTO>> emitir(@Valid @RequestBody EmitirEntradasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitir(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // GET /entradas?clienteId=1&eventoId=2  (both optional, at least one required)
    @GetMapping
    public ResponseEntity<List<EntradaDTO>> buscar(@RequestParam(required = false) Long clienteId,
                                                   @RequestParam(required = false) Long eventoId) {
        return ResponseEntity.ok(service.buscar(clienteId, eventoId));
    }

    // Client requests a refund (72h rule is checked in the service)
    @PostMapping("/{id}/devolucion")
    public ResponseEntity<EntradaDTO> solicitarDevolucion(@PathVariable Long id) {
        return ResponseEntity.ok(service.solicitarDevolucion(id));
    }

    // Admin approves or rejects: POST /entradas/5/devolucion/resolucion?aprobada=true
    @PostMapping("/{id}/devolucion/resolucion")
    public ResponseEntity<EntradaDTO> resolverDevolucion(@PathVariable Long id,
                                                         @RequestParam boolean aprobada) {
        return ResponseEntity.ok(service.resolverDevolucion(id, aprobada));
    }

    // Called by Evento when an event is cancelled: all sold tickets go to REINTEGRO
    @PatchMapping("/evento/{eventoId}/reintegro")
    public ResponseEntity<Map<String, Integer>> pasarAReintegroPorEvento(@PathVariable Long eventoId) {
        int afectadas = service.pasarAReintegroPorEvento(eventoId);
        return ResponseEntity.ok(Map.of("entradasEnReintegro", afectadas));
    }
}