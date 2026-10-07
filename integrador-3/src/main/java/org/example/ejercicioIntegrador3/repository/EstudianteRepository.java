package org.example.ejercicioIntegrador3.repository;

import org.example.ejercicioIntegrador3.dto.EstudianteDTO;
import org.example.ejercicioIntegrador3.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("EstudianteRepository")
public interface EstudianteRepository  extends JpaRepository<Estudiante,Integer> {

    @Query("SELECT new org.example.ejercicioIntegrador3.dto.EstudianteDTO(e.DNI, e.nombre, e.apellido, e.edad,e.genero, e.ciudad, e.LU) " +
            "FROM Estudiante e " +
            "JOIN e.listCarreras lc " +
            "JOIN lc.carrera c " +
            "WHERE e.ciudad = :ciudad AND c.nombre = :carrera")
    List<EstudianteDTO> recuperarEstudiantesPorCarreraYCiudad(@Param("ciudad") String ciudad,
                                                              @Param("carrera") String carrera);
}
