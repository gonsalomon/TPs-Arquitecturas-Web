package org.example.ejercicioIntegrador3.dto;

import lombok.Getter;
import lombok.Setter;

/* Una fila del reporte del punto h): una carrera, un año, y cuantos se
 * inscribieron y cuantos egresaron en ese año. */
@Getter
@Setter
public class ReporteCarreraDTO {
    private String carrera;
    private Integer anio;
    private Long inscriptos;
    private Long egresados;

    public ReporteCarreraDTO(String carrera, Integer anio) {
        this.carrera = carrera;
        this.anio = anio;
        this.inscriptos = 0L;
        this.egresados = 0L;
    }
}
