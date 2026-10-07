package org.example.ejercicioIntegrador3.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteDTO {
    private Integer dni;
    private String nombre;
    private String apellido;
    private Integer edad;
    private String genero;
    private String ciudad;
    private Integer lu;


    @Override
    public String toString() {
        return "Dni: " + dni + " " + nombre + " " + apellido + ", Género: " + genero + " LU: " + lu;
    }
}