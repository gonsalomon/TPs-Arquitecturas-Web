package org.example.reserva.repository;

import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.entity.Reserva;
import org.example.reserva.utils.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query(value = "SELECT * FROM reserva WHERE id = :idReserva", nativeQuery = true)
    ReservaResponseDTO buscarReserva(@Param("idReserva") Long idReserva);

    @Query(value = "SELECT * FROM reserva WHERE id_cliente = :idCliente AND id_evento = :idEvento AND estado IN (:estados)", nativeQuery = true)
    List<Reserva> buscarReservasPorCliente(
            @Param("idCliente") Long idCliente,
            @Param("idEvento") String idEvento,
            @Param("estados") List<String> estados
    );

    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.asientos a " +
            "WHERE r.idEvento = :idEvento " +
            "AND r.estado IN :estados " +
            "AND a IN :asientosSolicitados")
    long contarAsientosOcupados(
            @Param("idEvento") String idEvento, // O String, dependiendo de tu tipo de dato
            @Param("estados") List<String> estados,
            @Param("asientosSolicitados") List<Long> asientosSolicitados
    );

    List<Reserva> findByEstadoAndFechaVencimientoBefore(EstadoReserva estado, LocalDateTime fechaActual);
}
