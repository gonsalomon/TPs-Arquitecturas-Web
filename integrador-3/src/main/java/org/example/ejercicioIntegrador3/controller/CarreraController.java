package org.example.ejercicioIntegrador3.controller;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO;
import org.example.ejercicioIntegrador3.dto.CarreraDTO;
import org.example.ejercicioIntegrador3.repository.CarreraRepository;
import org.example.ejercicioIntegrador3.service.CarreraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carreras")
@AllArgsConstructor
public class CarreraController {

    private final CarreraService service;
    private final CarreraRepository carreraRepository;

    //GET ID: obtengo una carrera por su identificador
    @GetMapping("/{id}")
    public ResponseEntity<CarreraDTO> findById(@PathVariable Integer id) {
        CarreraDTO carrera = service.findById(id);
        return ResponseEntity.ok(carrera);
    }

    //f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos.
    @GetMapping("/carreras-inscriptos")
    public ResponseEntity<List<CarreraCantInscriptosDTO>> getTotalInscriptosCarreras() {
        List<CarreraCantInscriptosDTO> inscriptos = service.findConInscriptosOrdenadasPorCantidad();
        return ResponseEntity.ok(inscriptos);
    }
}
