package org.repository;

import org.dto.CarreraDTO;
import org.dto.EstudianteDTO;
import org.dto.ReporteDTO;
import org.entity.Carrera;

import java.util.List;

public interface CarreraRepository {
    public Carrera create(Carrera carrera);
    public Carrera findById(Integer idCarrera);
    public List<CarreraDTO> findConInscriptosOrdenadasPorCantidad();
    public List<ReporteDTO> generarReporteCarreras();
}
