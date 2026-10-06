package org.example.ejercicioIntegrador3.service;

import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.mapper.EstudianteMapper;
import org.example.ejercicioIntegrador3.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;
    private final EstudianteMapper mapper;


    //Completar
}
