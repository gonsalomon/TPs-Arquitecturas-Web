package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("InscripcionRepository")
public interface InscripcionRepository extends JpaRepository<Inscripcion,Integer> {
}
