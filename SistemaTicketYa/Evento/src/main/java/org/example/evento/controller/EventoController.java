package org.example.evento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.evento.dto.request.BusquedaEventoDTO;
import org.example.evento.dto.request.CambioEstadoDTO;
import org.example.evento.dto.request.EventoRequestDTO;
import org.example.evento.dto.response.EventoResponseDTO;
import org.example.evento.service.EventoService;
import org.example.evento.utils.EstadoEvento;
import org.example.evento.utils.OrdenBusqueda;
import org.example.evento.utils.TipoEvento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@AllArgsConstructor
@Tag(name = "Eventos", description = "Ciclo de vida de los eventos")
public class EventoController {
    private final EventoService service;

    @Operation(summary = "Crear un evento (queda en BORRADOR)")
    @PostMapping("")
    public ResponseEntity<EventoResponseDTO> create(@Valid @RequestBody EventoRequestDTO evento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(evento));
    }

    @Operation(summary = "Obtener eventos (opcional: ?estado=PUBLICADO)")
    @GetMapping("")
    public ResponseEntity<List<EventoResponseDTO>> getAll(@RequestParam(required = false) EstadoEvento estado) {
        return ResponseEntity.ok(service.findAll(estado));
    }

    @Operation(summary = "Buscar eventos a la venta por multiples criterios",
            description = "Todos los filtros son opcionales. orden: FECHA (default), PRECIO o RELEVANCIA. " +
                    "Ej: /api/eventos/buscar?tipo=RECITAL&ciudad=Tandil&desde=2026-12-01&hasta=2026-12-31&orden=PRECIO")
    @GetMapping("/buscar")
    public ResponseEntity<List<EventoResponseDTO>> buscar(
            @RequestParam(required = false) TipoEvento tipo,
            @RequestParam(required = false) Long idOrganizador,
            @RequestParam(required = false) String participante,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) OrdenBusqueda orden) {
        BusquedaEventoDTO filtro = new BusquedaEventoDTO(tipo, idOrganizador, participante, ciudad, desde, hasta, texto, orden);
        return ResponseEntity.ok(service.buscar(filtro));
    }

    @Operation(summary = "Obtener un evento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Actualizar un evento (solo en BORRADOR)")
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> update(@PathVariable String id, @Valid @RequestBody EventoRequestDTO evento) {
        return ResponseEntity.ok(service.update(id, evento));
    }

    @Operation(summary = "Cambiar el estado: publicar, cancelar, agotar o finalizar")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EventoResponseDTO> cambiarEstado(@PathVariable String id, @Valid @RequestBody CambioEstadoDTO body) {
        return ResponseEntity.ok(service.cambiarEstado(id, body.getEstado()));
    }

    @Operation(summary = "Eliminar un evento (solo en BORRADOR)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
