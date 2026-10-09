package org.example.ejercicioIntegrador3.mapper;

import org.example.ejercicioIntegrador3.dto.InscripcionDTO;
import org.example.ejercicioIntegrador3.entity.Inscripcion;
import org.springframework.stereotype.Component;

@Component
public class InscripcionMapper {

    public InscripcionDTO convertToDTO(Inscripcion entity) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(entity.getId());
        dto.setDni(entity.getEstudiante().getDNI());
        dto.setIdCarrera(entity.getCarrera().getIdCarrera());
        dto.setAnioInscripcion(entity.getInscripcion());
        dto.setAnioGraduacion(entity.getGraduacion());
        dto.setAntiguedad(entity.getAntiguedad());
        return dto;
    }
}
