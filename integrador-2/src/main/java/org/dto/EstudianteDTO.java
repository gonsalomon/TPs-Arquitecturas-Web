package org.dto;

import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class EstudianteDTO {
    private Integer DNI;
    private String nombre;
    private String apellido;
    private String genero;
    private Integer edad;
    private String ciudad;
    private Integer LU;

    @Override
    public String toString() {
        return "Dni: " + DNI + " " + nombre + " " + apellido + ", Género: " + genero + " LU: " + LU;
    }
}
