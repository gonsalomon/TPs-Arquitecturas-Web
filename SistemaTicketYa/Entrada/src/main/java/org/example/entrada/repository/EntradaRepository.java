package org.example.entrada.repository;

import jakarta.persistence.LockModeType;
import org.example.entrada.entity.Entrada;
import org.example.entrada.utils.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    List<Entrada> findByOrdenId(Long ordenId);

    List<Entrada> findByOrdenIdAndEstado(Long ordenId, EstadoEntrada estado);

    List<Entrada> findByClienteId(Long clienteId);

    List<Entrada> findByEventoId(String eventoId);

    List<Entrada> findByClienteIdAndEventoId(Long clienteId, String eventoId);

    List<Entrada> findByEventoIdAndEstado(String eventoId, EstadoEntrada estado);

    long countByClienteIdAndEventoIdAndEstadoIn(Long clienteId, String eventoId, Collection<EstadoEntrada> estados);

    Optional<Entrada> findByCodigoQr(String codigoQr);
    //para evitar validaciones simultáneas, se agregar un bloqueo pesimista
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Entrada e WHERE e.codigoQr = :codigoQr")
    Optional<Entrada> buscarPorCodigoQrConBloqueo(
            @Param("codigoQr") String codigoQr
    );

    @Query("""
    SELECT e2.eventoId
    FROM Entrada e1
    JOIN Entrada e2 ON e1.clienteId = e2.clienteId
    WHERE e1.eventoId = :eventoId
      AND e2.eventoId <> :eventoId
      AND e1.estado <> :estadoExcluido
      AND e2.estado <> :estadoExcluido
    GROUP BY e2.eventoId
    ORDER BY COUNT(DISTINCT e2.clienteId) DESC
""")
    List<String> findEventosCoComprados(
            @Param("eventoId") String eventoId,
            @Param("estadoExcluido") EstadoEntrada estadoExcluido,
            Pageable pageable
    );

    @Query("""
    SELECT e2.eventoId
    FROM Entrada e1
    JOIN Entrada e2 ON e1.clienteId = e2.clienteId
    WHERE e1.eventoId IN (
        SELECT ec.eventoId
        FROM Entrada ec
        WHERE ec.clienteId = :clienteId
          AND ec.estado <> :estadoExcluido
    )
      AND e1.clienteId <> :clienteId
      AND e2.eventoId NOT IN (
        SELECT ec2.eventoId
        FROM Entrada ec2
        WHERE ec2.clienteId = :clienteId
          AND ec2.estado <> :estadoExcluido
    )
      AND e1.estado <> :estadoExcluido
      AND e2.estado <> :estadoExcluido
    GROUP BY e2.eventoId
    ORDER BY COUNT(DISTINCT e2.clienteId) DESC
""")
    List<String> findRecomendadosPorHistorialCliente(
            @Param("clienteId") Long clienteId,
            @Param("estadoExcluido") EstadoEntrada estadoExcluido,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT e.eventoId
    FROM Entrada e
    WHERE e.clienteId = :clienteId
      AND e.estado <> :estadoExcluido
""")
    List<String> findEventosCompradosPorCliente(
            @Param("clienteId") Long clienteId,
            @Param("estadoExcluido") EstadoEntrada estadoExcluido
    );
    List<Entrada> findByEstadoAndOrdenId(EstadoEntrada estado, Long ordenId);
}