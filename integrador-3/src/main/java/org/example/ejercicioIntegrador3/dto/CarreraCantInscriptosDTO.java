package org.example.ejercicioIntegrador3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarreraCantInscriptosDTO {
    private String nombre;
    private Long cantidadInscriptos;

    public CarreraCantInscriptosDTO(String nombre, Long cantidadInscriptos) {
        this.nombre = nombre;
        this.cantidadInscriptos = cantidadInscriptos;
    }
}