package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.repository.EstudianteRepository;
import org.example.ejercicioIntegrador3.service.EstudianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estudiantes")
@AllArgsConstructor
public class EstudianteController {
    private final EstudianteService service;
    private final EstudianteRepository estudianteRepository;

    //g) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia.
    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> recuperarEstudiantesPorCarreraYCiudad(@RequestParam("ciudad") String ciudad, @RequestParam("carrera") String carrera){
        List<EstudianteDTO> estudiantes = service.recuperarEstudiantesPorCarreraYCiudad(ciudad,carrera);
        return ResponseEntity.ok(estudiantes);
    }
}
