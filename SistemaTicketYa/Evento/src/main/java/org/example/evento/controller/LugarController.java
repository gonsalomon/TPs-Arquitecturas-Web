package org.example.evento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.evento.dto.request.LugarRequestDTO;
import org.example.evento.dto.response.LugarResponseDTO;
import org.example.evento.service.LugarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lugares")
@AllArgsConstructor
@Tag(name = "Lugares", description = "Estadios, teatros y salas con sus sectores")
public class LugarController {
    private final LugarService service;

    @Operation(summary = "Crear un lugar con sus sectores")
    @PostMapping("")
    public ResponseEntity<LugarResponseDTO> create(@Valid @RequestBody LugarRequestDTO lugar) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(lugar));
    }

    @Operation(summary = "Obtener todos los lugares")
    @GetMapping("")
    public ResponseEntity<List<LugarResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Obtener un lugar por ID")
    @GetMapping("/{id}")
    public ResponseEntity<LugarResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Buscar un lugar por nombre")
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<LugarResponseDTO> getByNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(service.findByNombre(nombre));
    }

    @Operation(summary = "Actualizar un lugar")
    @PutMapping("/{id}")
    public ResponseEntity<LugarResponseDTO> update(@PathVariable String id, @Valid @RequestBody LugarRequestDTO lugar) {
        return ResponseEntity.ok(service.update(id, lugar));
    }

    @Operation(summary = "Eliminar un lugar (solo si no tiene eventos)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
