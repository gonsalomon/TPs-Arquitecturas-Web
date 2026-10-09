package org.example.ejercicioIntegrador3.service;
import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.InscripcionDTO;
import org.example.ejercicioIntegrador3.entity.Carrera;
import org.example.ejercicioIntegrador3.entity.Estudiante;
import org.example.ejercicioIntegrador3.entity.Inscripcion;
import org.example.ejercicioIntegrador3.mapper.InscripcionMapper;
import org.example.ejercicioIntegrador3.repository.CarreraRepository;
import org.example.ejercicioIntegrador3.repository.EstudianteRepository;
import org.example.ejercicioIntegrador3.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@AllArgsConstructor
@Service
public class InscripcionService {
    private final EstudianteRepository estudianteRepository;
    private final CarreraRepository carreraRepository;
    private final InscripcionRepository inscripcionRepository;
    private final InscripcionMapper mapper;

    //b) matricular un estudiante en una carrera
    @Transactional
    public InscripcionDTO matricular(InscripcionDTO  request) {
        Integer dni = request.getDni();
        Integer idCarrera = request.getIdCarrera();
        Estudiante estudiante = estudianteRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un estudiante con el DNI: " + dni));
        Carrera carrera = carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una carrera con el id: " +idCarrera));
        if (inscripcionRepository.existeInscripcion(dni, idCarrera)) {
            throw new IllegalArgumentException("El estudiante ya está matriculado en esa carrera");
        }
        Integer anioInscripcion =  LocalDate.now().getYear();
        Inscripcion inscripcion = new Inscripcion(carrera, estudiante, anioInscripcion, 0, 1);
        Inscripcion nueva = inscripcionRepository.save(inscripcion);
        return mapper.convertToDTO(nueva);
    }

}
