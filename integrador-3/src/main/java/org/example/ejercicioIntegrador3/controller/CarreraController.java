package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.CarreraDTO;
import org.example.ejercicioIntegrador3.service.CarreraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carreras")
@AllArgsConstructor
public class CarreraController {

    private final CarreraService service;

    //GET ID: obtengo una carrera por su identificador
    @GetMapping("/{id}")
    public ResponseEntity<CarreraDTO> findById(@PathVariable Integer id) {
        CarreraDTO carrera = service.findById(id);
        return ResponseEntity.ok(carrera);
    }
}
