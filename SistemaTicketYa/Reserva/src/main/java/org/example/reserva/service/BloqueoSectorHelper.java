package org.example.reserva.service;

import lombok.RequiredArgsConstructor;
import org.example.reserva.entity.BloqueoSector;
import org.example.reserva.repository.BloqueoSectorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BloqueoSectorHelper {

    private final BloqueoSectorRepository bloqueoSectorRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asegurarFilaExiste(String idEvento, String sector) {
        try {
            bloqueoSectorRepository.saveAndFlush(new BloqueoSector(null, idEvento, sector));
        } catch (DataIntegrityViolationException ignored) {
            // Otra instancia ya insertó la fila en paralelo
        }
    }
}
