package org.example.ejercicioIntegrador3.mapper;

import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.entity.Estudiante;
import org.springframework.stereotype.Component;

@Component
public class EstudianteMapper {

    public static Estudiante convertToEntity(EstudianteDTO dto){
        return new Estudiante(
                dto.getDNI(),
                dto.getNombre(),
                dto.getApellido(),
                dto.getEdad(),
                dto.getGenero(),
                dto.getCiudad(),
                dto.getLU()
        );
    }

    public EstudianteDTO convertToDTO(Estudiante entity){
        EstudianteDTO dto = new EstudianteDTO();
        dto.setDNI(entity.getDNI());
        dto.setNombre(entity.getNombre());
        dto.setApellido(entity.getApellido());
        dto.setGenero(entity.getGenero());
        dto.setEdad(entity.getEdad());
        dto.setCiudad(entity.getCiudad());
        dto.setLU(entity.getLU());
        return dto;
    }
}
