package org.example.ejercicioIntegrador3.service;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.mapper.EstudianteMapper;
import org.example.ejercicioIntegrador3.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;
    private final EstudianteMapper mapper;


    //Completar
    @Transactional(readOnly = true)
    public List<EstudianteDTO> recuperarEstudiantesPorCarreraYCiudad(String ciudad, String Carrera) {
        return estudianteRepository.recuperarEstudiantesPorCarreraYCiudad(ciudad,Carrera);
    }

    //c) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple.
    @Transactional(readOnly = true)
    public List<EstudianteDTO> recuperarTodosLosEstudiantes() {
        return estudianteRepository.recuperarTodosLosEstudiantes();
    }

    //d) recuperar un estudiante, en base a su número de libreta universitaria.
    @Transactional(readOnly = true)
    public EstudianteDTO recuperarEstudiantePorLU(Integer lu) {
        return estudianteRepository.recuperarEstudiantePorLU(lu)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un estudiante con el LU: " + lu));
    }
}
