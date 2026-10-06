package org.example.ejercicioIntegrador3.dto;

import lombok.*;
import org.example.ejercicioIntegrador3.entity.Carrera;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CarreraDTO {
    private Integer idCarrera;
    private String nombre;
    private Integer duracion;

    public CarreraDTO(Carrera carrera) {
        this.idCarrera = carrera.getIdCarrera();
        this.nombre = carrera.getNombre();
        this.duracion = carrera.getDuracion();
    }
}
