package org.repository;

import org.entity.Carrera;

public interface CarreraRepository {
    public Carrera create(Carrera carrera);
    public Carrera findById(Integer idCarrera);
}
