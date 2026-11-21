package org.example.entrada.repository;

import jakarta.persistence.LockModeType;
import org.example.entrada.entity.Entrada;
import org.example.entrada.utils.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}