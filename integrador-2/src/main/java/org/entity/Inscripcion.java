package org.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Inscripcion {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_estudiante")
    private Estudiante estudiante;
    @Column
    private Integer inscripcion;
    @Column
    private Integer graduacion;
    @Column
    private Integer antiguedad;


    public Inscripcion(Carrera c, Estudiante e, Integer fechaInscripcion, Integer fechaGraduacion, Integer antiguedad) {
        this.carrera = c;
        this.estudiante = e;
        this.inscripcion = fechaInscripcion;
        this.graduacion = fechaGraduacion;
        this.antiguedad = antiguedad;
    }
}
