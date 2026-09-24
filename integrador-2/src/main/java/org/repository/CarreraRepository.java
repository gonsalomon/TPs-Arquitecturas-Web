package org.repository;

import org.dto.ReporteDTO;
import org.entity.Carrera;

import java.util.List;

public interface CarreraRepository {
    public Carrera create(Carrera carrera);
    public Carrera findById(Integer idCarrera);

    /**
     * Punto 3: reporte de carreras con inscriptos y egresados por año.
     * Las carreras se devuelven ordenadas alfabéticamente y, dentro de cada
     * una, los años en orden cronológico.
     */
    public List<ReporteDTO> generarReporteCarreras();
}
