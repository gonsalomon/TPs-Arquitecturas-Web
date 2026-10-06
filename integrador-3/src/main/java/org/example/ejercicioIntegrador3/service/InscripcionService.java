package org.example.ejercicioIntegrador3.service;
import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.mapper.InscripcionMapper;
import org.example.ejercicioIntegrador3.repository.CarreraRepository;
import org.example.ejercicioIntegrador3.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class InscripcionService {
    private final EstudianteRepository estudianteRepository;
    private final CarreraRepository carreraRepository;
    private final InscripcionMapper mapper;

    //Completar

}
