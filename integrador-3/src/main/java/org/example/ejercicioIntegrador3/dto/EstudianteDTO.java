package org.example.ejercicioIntegrador3.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteDTO {
    private Integer DNI;
    private String nombre;
    private String apellido;
    private Integer edad;
    private String genero;
    private String ciudad;
    private Integer LU;


    @Override
    public String toString() {
        return "Dni: " + DNI + " " + nombre + " " + apellido + ", Género: " + genero + " LU: " + LU;
    }
}