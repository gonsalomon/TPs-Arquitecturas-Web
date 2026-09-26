package org.repository;

import org.dto.InscripcionDTO;
import org.entity.Inscripcion;


public interface InscripcionRepository {
    public Inscripcion create(Integer idCarrera, Integer idEstudiante, Integer fechaInscripcion, Integer fechaGraduacion, Integer antiguedad);
    void save(Inscripcion nueva);
}
