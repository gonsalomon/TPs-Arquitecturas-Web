package org.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarreraDTO {
    private String nombre;
    private Long cantidadInscriptos;

    @Override
    public String toString() {
        return "Carrera: " + nombre + ", inscriptos: " + cantidadInscriptos;
    }
}
