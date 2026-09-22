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
public class Carrera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_carrera")
    private Integer idCarrera;

    @Column
    private String nombre;

    @Column
    private Integer duracion;

    @OneToMany (mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Inscripcion> alumnosInscriptos;


    public Carrera(Integer idCarrera, String  nombre, Integer duracion) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.duracion = duracion;
        this.alumnosInscriptos = new ArrayList<Inscripcion>();
    }

    public Carrera(String nombre, Integer duracion) {
        this.nombre = nombre;
        this.duracion = duracion;
    }
}
