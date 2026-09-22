package org.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Estudiante {

    @Id
    @Column
    private Integer DNI;
    @Column
    private String nombre;
    @Column
    private String apellido;
    @Column
    private String genero;
    @Column
    private Integer edad;
    @Column
    private String ciudad;
    @Column
    private Integer LU;

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<Inscripcion> listCarreras;

    public Estudiante(Integer dni, String nombre, String apellido,Integer edad, String genero, String ciudad, Integer LU) {
        this.DNI = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.genero = genero;
        this.edad = edad;
        this.ciudad = ciudad;
        this.LU = LU;
        this.listCarreras = new ArrayList<Inscripcion>();

    }

}
