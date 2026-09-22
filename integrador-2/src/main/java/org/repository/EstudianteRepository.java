package org.repository;

import org.entity.Estudiante;

public interface EstudianteRepository {
    public Estudiante create(Estudiante estudiante);
    public Estudiante findByDni(Integer dni);
}
