package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.dto.InscripcionDTO;
import org.example.ejercicioIntegrador3.service.InscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inscripciones")
@AllArgsConstructor
public class InscripcionController {
    private final InscripcionService service;

    //b) matricular un estudiante en una carrera
    @PostMapping("")
    public ResponseEntity<InscripcionDTO> matricular(@RequestBody InscripcionDTO request) {
        InscripcionDTO nuevo = service.matricular(request);
        return ResponseEntity.ok(nuevo);
    }
}
