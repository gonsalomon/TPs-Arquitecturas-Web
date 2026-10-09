package org.example.ejercicioIntegrador3.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionDTO {
    private Integer id;
    private Integer dni;
    private Integer idCarrera;
    private Integer anioInscripcion;
    private Integer anioGraduacion;
    private Integer antiguedad;
}
