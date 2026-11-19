package org.example.fila_virtual.repository;


import org.example.fila_virtual.entity.TurnoFila;
import org.example.fila_virtual.utils.EstadoTurno;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TurnoFilaRepository extends JpaRepository<TurnoFila, Long> {

    // Busca si el cliente ya tiene un turno activo (EN_ESPERA o HABILITADO) para ese evento
    Optional<TurnoFila> findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
            String eventoId, Long clienteId, List<EstadoTurno> estados);

    // Cuenta cuántos usuarios hay actualmente en un estado dado para un evento
    long countByEventoIdAndEstado(String eventoId, EstadoTurno estado);

    // Obtiene los próximos N clientes en estricto orden FIFO
    List<TurnoFila> findByEventoIdAndEstadoOrderByFechaIngresoAscIdAsc(
            String eventoId, EstadoTurno estado, Pageable pageable);

    // Calcula cuántas personas están delante de un turno específico
    @Query("SELECT COUNT(t) FROM TurnoFila t " +
            "WHERE t.eventoId = :eventoId " +
            "AND t.estado = :estado " +
            "AND (t.fechaIngreso < :fechaIngreso OR (t.fechaIngreso = :fechaIngreso AND t.id < :id))")
    long contarDelante(
            @Param("eventoId") String eventoId,
            @Param("estado") EstadoTurno estado,
            @Param("fechaIngreso") LocalDateTime fechaIngreso,
            @Param("id") Long id);

    // Busca turnos habilitados cuya ventana de compra ya venció
    List<TurnoFila> findByEstadoAndFechaExpiracionBefore(EstadoTurno estado, LocalDateTime ahora);

    // Devuelve todos los eventos que tienen gente esperando para procesar sus tandas
    @Query("SELECT DISTINCT t.eventoId FROM TurnoFila t WHERE t.estado = :estado")
    List<String> findEventosConEstado(@Param("estado") EstadoTurno estado);
}
