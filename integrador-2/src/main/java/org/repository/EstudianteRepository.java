package org.repository;

import org.dto.EstudianteDTO;
import org.entity.Estudiante;

import java.util.List;

public interface EstudianteRepository {
    public Estudiante create(Estudiante estudiante);
    public Estudiante findByDni(Integer dni);
    public List<EstudianteDTO> findByGender(String genero);
    public List<EstudianteDTO> findAllOrderByApellido();
    public List<EstudianteDTO> buscarPorCarreraYCiudad(Integer carreraId, String ciudad);
    public Estudiante findByLU(Integer lu);
}
