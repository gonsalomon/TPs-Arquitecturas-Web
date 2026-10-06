package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("EstudianteRepository")
public interface EstudianteRepository  extends JpaRepository<Estudiante,Integer> {
}
