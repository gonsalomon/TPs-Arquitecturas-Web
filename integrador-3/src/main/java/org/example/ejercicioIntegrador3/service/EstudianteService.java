package org.example.ejercicioIntegrador3.service;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.entity.Estudiante;
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

    //a)dar de alta un estudiante
    @Transactional
    public EstudianteDTO save(EstudianteDTO request){
        if(request.getDNI() ==null){
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        if(estudianteRepository.existsById(request.getDNI())) {
            throw new IllegalArgumentException("El DNI existe en el sistema");
        }
        if(request.getLU() ==null){
            throw new IllegalArgumentException("El LU es obligatorio");
        }
        //aca convierto de EstudianteDTO → Estudiante porque mi repository necesita un Estudiante
        Estudiante estudiante= mapper.convertToEntity(request);
        Estudiante nuevo = estudianteRepository.save(estudiante);
        //aca lo convierto de Estudiante → EstudianteDTO porque mi metodo devuelve un EstudianteDTO
        return mapper.convertToDTO(nuevo);
    }


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
