package org.repository;

import org.entity.Inscripcion;


public interface InscripcionRepository {
//    public Inscripcion create(Integer id, Integer idEstudiante, Integer idCarrera, Integer fechaInscripcion, Integer fechaGraduacion, Integer antiguedad);

    void save(Inscripcion nueva);
}
