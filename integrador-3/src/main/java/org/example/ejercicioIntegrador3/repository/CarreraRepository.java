package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("CarreraRepository")
// JpaRepository: el primer parámetro es la entidad y el segundo, el tipo de la PK
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

}
