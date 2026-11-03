package org.example.entrada.service;

import lombok.AllArgsConstructor;
import org.example.entrada.dto.EmitirEntradasRequest;
import org.example.entrada.dto.EntradaDTO;
import org.example.entrada.entity.Entrada;
import org.example.entrada.exception.AsientoNoDisponibleException;
import org.example.entrada.exception.DevolucionFueraDePlazoException;
import org.example.entrada.exception.LimiteEntradasExcedidoException;
import org.example.entrada.mapper.EntradaMapper;
import org.example.entrada.repository.EntradaRepository;
import org.example.entrada.utils.EstadoEntrada;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@AllArgsConstructor
public class EntradaService{
    private final EntradaRepository repo;
    private final EntradaMapper mapper;

    private static final int MAX_POR_CLIENTE = 6; // move to application.yml
    private static final List<EstadoEntrada> ACTIVAS =
            List.of(EstadoEntrada.VALIDA, EstadoEntrada.USADA, EstadoEntrada.EN_DEVOLUCION);

    // NO @Transactional here: if the insert fails, the tx is rollback-only and we can't re-query inside it.
    public List<EntradaDTO> emitir(EmitirEntradasRequest req) {
        List<Entrada> previas = repo.findByOrdenId(req.ordenId());
        if (!previas.isEmpty()) return mapper.toDtos(previas);            // payment retry -> same tickets

        long actuales = repo.countByClienteIdAndEventoIdAndEstadoIn(req.clienteId(), req.eventoId(), ACTIVAS);
        if (actuales + req.items().size() > MAX_POR_CLIENTE)
            throw new LimiteEntradasExcedidoException("Límite de entradas por cliente excedido");

        List<Entrada> nuevas = new ArrayList<>();
        for (int i = 0; i < req.items().size(); i++) {
            var it = req.items().get(i);
            Entrada e = new Entrada();
            e.setEventoId(req.eventoId());
            e.setSectorId(it.sectorId());
            e.setClienteId(req.clienteId());
            e.setOrdenId(req.ordenId());
            e.setFila(it.fila());
            e.setAsiento(it.asiento());
            e.setPrecio(it.precio());
            e.setFechaEvento(req.fechaEvento());
            e.setClaveIdempotencia(req.ordenId() + "-" + i);
            nuevas.add(e);
        }
        try {
            return mapper.toDtos(repo.saveAllAndFlush(nuevas));            // own tx, all-or-nothing
        } catch (DataIntegrityViolationException ex) {
            List<Entrada> existentes = repo.findByOrdenId(req.ordenId());   // concurrent duplicate retry won
            if (!existentes.isEmpty()) return mapper.toDtos(existentes);
            throw new AsientoNoDisponibleException("El asiento ya fue vendido");
        }
    }

    @Transactional(readOnly = true)
    public EntradaDTO obtenerPorId(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<EntradaDTO> buscar(Long clienteId, Long eventoId) {
        if (clienteId == null && eventoId == null)
            throw new IllegalArgumentException("Debe indicar clienteId o eventoId");
        List<Entrada> r = (clienteId != null && eventoId != null)
                ? repo.findByClienteIdAndEventoId(clienteId, eventoId)
                : (clienteId != null ? repo.findByClienteId(clienteId) : repo.findByEventoId(eventoId));
        return mapper.toDtos(r);
    }

    @Transactional
    public int pasarAReintegroPorEvento(Long eventoId) {
        return repo.cambiarEstadoPorEvento(eventoId, EstadoEntrada.REINTEGRO,
                List.of(EstadoEntrada.VALIDA, EstadoEntrada.EN_DEVOLUCION));
    }

    @Transactional
    public EntradaDTO solicitarDevolucion(Long id) {
        Entrada e = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));
        if (e.getEstado() != EstadoEntrada.VALIDA)
            throw new IllegalStateException("Solo entradas válidas pueden devolverse");
        if (LocalDateTime.now().isAfter(e.getFechaEvento().minusHours(72)))
            throw new DevolucionFueraDePlazoException("Fuera del plazo de 72 horas");
        e.setEstado(EstadoEntrada.EN_DEVOLUCION);
        return mapper.toDto(e);
    }

    @Transactional
    public EntradaDTO resolverDevolucion(Long id, boolean aprobada) {
        Entrada e = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));
        if (e.getEstado() != EstadoEntrada.EN_DEVOLUCION) throw new IllegalStateException("No hay devolución pendiente");
        e.setEstado(aprobada ? EstadoEntrada.REINTEGRO : EstadoEntrada.VALIDA); // @PreUpdate frees/relocks the seat key
        return mapper.toDto(e);
    }
}