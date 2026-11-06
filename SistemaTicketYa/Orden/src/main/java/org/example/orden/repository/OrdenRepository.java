package org.example.orden.repository;

import org.example.orden.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("OrdenRepository")
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @Query("SELECT COALESCE(SUM(SIZE(o.items)), 0) FROM Orden o " +
            "WHERE o.clienteId = :clienteId AND o.eventoId = :eventoId " +
            "AND o.estado IN (org.example.orden.utils.EstadoOrden.PENDIENTE_PAGO, " +
            "org.example.orden.utils.EstadoOrden.PAGO_APROBADO, " +
            "org.example.orden.utils.EstadoOrden.CONFIRMADA)")
    long contarEntradasActivasPorClienteYEvento(@Param("clienteId") Long clienteId,
                                                 @Param("eventoId") String eventoId);

    @Query("SELECT o FROM Orden o WHERE o.estado = org.example.orden.utils.EstadoOrden.PENDIENTE_PAGO " +
            "AND o.fechaVencimiento < :ahora")
    List<Orden> buscarPendientesVencidas(@Param("ahora") LocalDateTime ahora);

    List<Orden> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    List<Orden> findByClienteIdAndEventoIdOrderByFechaCreacionDesc(Long clienteId, String eventoId);

    Optional<Orden> findByIdOrdenAndClienteId(Long idOrden, Long clienteId);
}
