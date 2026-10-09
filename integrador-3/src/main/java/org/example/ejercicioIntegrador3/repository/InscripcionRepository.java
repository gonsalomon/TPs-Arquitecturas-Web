package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("InscripcionRepository")
public interface InscripcionRepository extends JpaRepository<Inscripcion,Integer> {

    //b) matricular un estudiante en una carrera: evitar doble matrícula
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.estudiante.DNI = :dni AND i.carrera.idCarrera = :idCarrera")
    boolean existeInscripcion(@Param("dni") Integer dni, @Param("idCarrera") Integer idCarrera);
}
