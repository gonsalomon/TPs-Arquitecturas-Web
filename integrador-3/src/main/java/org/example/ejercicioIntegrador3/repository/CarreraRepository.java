package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO;
import org.example.ejercicioIntegrador3.entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("CarreraRepository")
// JpaRepository: el primer parámetro es la entidad y el segundo, el tipo de la PK
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    //f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos.
    @Query("SELECT new org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO(c.nombre, COUNT(i)) " +
            "FROM Carrera c JOIN c.alumnosInscriptos i " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(i) DESC")
    List<CarreraCantInscriptosDTO> findConInscriptosOrdenadasPorCantidad();

}
