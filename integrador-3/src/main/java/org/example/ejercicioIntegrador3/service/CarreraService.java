package org.example.ejercicioIntegrador3.service;


import lombok.AllArgsConstructor;
import org.example.ejercicioIntegrador3.dto.CarreraCantInscriptosDTO;
import org.example.ejercicioIntegrador3.dto.CarreraDTO;
import org.example.ejercicioIntegrador3.entity.Carrera;
import org.example.ejercicioIntegrador3.mapper.CarreraMapper;
import org.example.ejercicioIntegrador3.repository.CarreraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class CarreraService {
    private final CarreraRepository carreraRepository;
    private final CarreraMapper mapper;
    //@Transactional(readOnly = true) → consultas.
    //@Transactional → operaciones que crean, modifican o eliminan datos.
    @Transactional(readOnly = true) //se usa para indicar que un método debe ejecutarse dentro de una transacción de base de datos.
    public CarreraDTO findById(Integer id) {
        Carrera c = carreraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una carrera con el id: " + id));
        return mapper.convertToDTO(c);
    }

    @Transactional(readOnly = true)
    public List<CarreraCantInscriptosDTO> findConInscriptosOrdenadasPorCantidad() {
        return carreraRepository.findConInscriptosOrdenadasPorCantidad();
    }

}
