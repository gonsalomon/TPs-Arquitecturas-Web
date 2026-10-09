package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.InscripcionDTO;
import org.example.ejercicioIntegrador3.service.InscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estudiantes")
@AllArgsConstructor
public class InscripcionController {
    private final InscripcionService service;

    //b) matricular un estudiante en una carrera
    @PostMapping("/{dni}/carreras/{idCarrera}")
    public ResponseEntity<InscripcionDTO> matricular(@PathVariable Integer dni,
                                                       @PathVariable Integer idCarrera,
                                                       @RequestBody InscripcionDTO request) {
        InscripcionDTO nueva = service.matricular(dni, idCarrera, request.getAnioInscripcion());
        return ResponseEntity.ok(nueva);
    }
}
