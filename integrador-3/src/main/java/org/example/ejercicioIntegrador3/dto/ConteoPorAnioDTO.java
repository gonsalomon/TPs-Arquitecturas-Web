package org.example.ejercicioIntegrador3.dto;

import lombok.Getter;
import lombok.Setter;

/* Resultado intermedio de las consultas del punto h): cuantos registros hay
 * para una carrera en un año. Se usa tanto para contar inscriptos (agrupando
 * por año de inscripción) como egresados (agrupando por año de graduación). */
@Getter
@Setter
public class ConteoPorAnioDTO {
    private String carrera;
    private Integer anio;
    private Long cantidad;

    public ConteoPorAnioDTO(String carrera, Integer anio, Long cantidad) {
        this.carrera = carrera;
        this.anio = anio;
        this.cantidad = cantidad;
    }
}
