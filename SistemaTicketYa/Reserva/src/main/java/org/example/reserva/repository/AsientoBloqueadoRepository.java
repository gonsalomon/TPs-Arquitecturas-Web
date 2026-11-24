package org.example.reserva.repository;

import org.example.reserva.entity.AsientoBloqueado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsientoBloqueadoRepository extends JpaRepository<AsientoBloqueado, Long> {

    void deleteByIdReserva(Long idReserva);

    void deleteByIdReservaIn(List<Long> idsReserva);
}
