package org.entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Inscripcion {
    @Id
    @Column
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

}
