package org.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Estudiante {
    @Id
    private Integer dni;
    private String nombre;
    private String apellido;
    private String genero;
    private Integer edad;
    private String ciudad;
    private Integer lu;

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<Inscripcion> listCarreras = new ArrayList<>();

    public Estudiante(Integer dni, String nombre, String apellido, Integer edad,
                      String genero, String ciudad, Integer lu) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.genero = genero;
        this.ciudad = ciudad;
        this.lu = lu;
    }
}