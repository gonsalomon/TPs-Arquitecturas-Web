package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO;
import org.example.ejercicioIntegrador3.dto.ConteoPorAnioDTO;
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

    //h) cuantos estudiantes se inscribieron en cada carrera, agrupados por año de inscripción
    @Query("SELECT new org.example.ejercicioIntegrador3.dto.ConteoPorAnioDTO(c.nombre, i.inscripcion, COUNT(i)) " +
            "FROM Inscripcion i JOIN i.carrera c " +
            "WHERE i.inscripcion IS NOT NULL " +
            "GROUP BY c.nombre, i.inscripcion " +
            "ORDER BY c.nombre ASC, i.inscripcion ASC")
    List<ConteoPorAnioDTO> contarInscriptosPorCarreraYAnio();

    //h) cuantos egresaron de cada carrera, agrupados por año de graduación.
    //   graduacion = 0 es el valor que usan los datos para "todavía no egresó".
    @Query("SELECT new org.example.ejercicioIntegrador3.dto.ConteoPorAnioDTO(c.nombre, i.graduacion, COUNT(i)) " +
            "FROM Inscripcion i JOIN i.carrera c " +
            "WHERE i.graduacion IS NOT NULL AND i.graduacion <> 0 " +
            "GROUP BY c.nombre, i.graduacion " +
            "ORDER BY c.nombre ASC, i.graduacion ASC")
    List<ConteoPorAnioDTO> contarEgresadosPorCarreraYAnio();

}
