package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.entity.Estudiante;
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

    //a)dar de alta un estudiante
    @PostMapping("")
    public ResponseEntity<EstudianteDTO> creataEstudiante(@RequestBody EstudianteDTO estudiante) {
        EstudianteDTO nuevo = service.save(estudiante);
        return ResponseEntity.ok(nuevo);
    }
    //g) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia.
    @GetMapping("/filtro")
    public ResponseEntity<List<EstudianteDTO>> recuperarEstudiantesPorCarreraYCiudad(@RequestParam("ciudad") String ciudad, @RequestParam("carrera") String carrera){
        List<EstudianteDTO> estudiantes = service.recuperarEstudiantesPorCarreraYCiudad(ciudad,carrera);
        return ResponseEntity.ok(estudiantes);
    }

    //c) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple.
    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> recuperarTodosLosEstudiantes(){
        List<EstudianteDTO> estudiantes = service.recuperarTodosLosEstudiantes();
        return ResponseEntity.ok(estudiantes);
    }

    //d) recuperar un estudiante, en base a su número de libreta universitaria.
    @GetMapping("/lu/{lu}")
    public ResponseEntity<EstudianteDTO> recuperarEstudiantePorLU(@PathVariable Integer lu){
        EstudianteDTO estudiante = service.recuperarEstudiantePorLU(lu);
        return ResponseEntity.ok(estudiante);
    }
}
