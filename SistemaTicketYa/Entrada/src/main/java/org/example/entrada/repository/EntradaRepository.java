package org.example.entrada.repository;

import org.example.entrada.entity.Entrada;
import org.example.entrada.utils.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    List<Entrada> findByOrdenId(Long ordenId);

    List<Entrada> findByOrdenIdAndEstado(Long ordenId, EstadoEntrada estado);

    List<Entrada> findByClienteId(Long clienteId);

    List<Entrada> findByEventoId(String eventoId);

    List<Entrada> findByClienteIdAndEventoId(Long clienteId, String eventoId);

    List<Entrada> findByEventoIdAndEstado(String eventoId, EstadoEntrada estado);

    long countByClienteIdAndEventoIdAndEstadoIn(Long clienteId, String eventoId, Collection<EstadoEntrada> estados);
}