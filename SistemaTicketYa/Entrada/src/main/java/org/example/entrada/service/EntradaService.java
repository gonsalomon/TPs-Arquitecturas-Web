package org.example.entrada.service;

import org.example.entrada.client.PagoFeignClient;
import org.example.entrada.client.ReservaFeignClient;
import org.example.entrada.client.UsuarioFeignClient;
import org.example.entrada.client.usuario.response.dto.UsuarioResponseDTO;
import org.example.entrada.client.utils.Rol;
import org.example.entrada.dto.*;
import org.example.entrada.dto.response.EntradaEmitidaDTO;
import org.example.entrada.entity.Entrada;
import org.example.entrada.exception.AsientoNoDisponibleException;
import org.example.entrada.exception.DevolucionFueraDePlazoException;
import org.example.entrada.exception.LimiteEntradasExcedidoException;
import org.example.entrada.mapper.EntradaMapper;
import org.example.entrada.repository.EntradaRepository;
import org.example.entrada.utils.EstadoEntrada;
import org.example.entrada.utils.EstadoEvento;
import org.example.entrada.utils.MotivoCambio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entrada owns sold tickets and their invariants. It makes no calls to other services:
 * Orden drives purchase, refund and cancellation, and uses these methods.
 */
@Service
public class EntradaService {

    private static final List<EstadoEntrada> ACTIVAS = List.of(EstadoEntrada.VALIDA, EstadoEntrada.USADA);
    private static final int HORAS_LIMITE_DEVOLUCION = 72;

    private final EntradaRepository repo;
    private final EntradaMapper mapper;
    private final int maxPorCliente;
    private final UsuarioFeignClient usuarioFeignClient;
    private final PagoFeignClient  pagoFeignClient;
    private final ReservaFeignClient reservaFeignClient;

    public EntradaService(EntradaRepository repo,
                          EntradaMapper mapper,
                          @Value("${entrada.max-por-cliente:6}") int maxPorCliente,
                          UsuarioFeignClient usuarioFeignClient,
                          PagoFeignClient  pagoFeignClient,
                          ReservaFeignClient reservaFeignClient) {
        this.repo = repo;
        this.mapper = mapper;
        this.maxPorCliente = maxPorCliente;
        this.usuarioFeignClient = usuarioFeignClient;
        this.pagoFeignClient = pagoFeignClient;
        this.reservaFeignClient = reservaFeignClient;
    }

    // ---------------------------------------------------------------- issuance

    // NOT @Transactional on purpose: saveAllAndFlush runs in its own transaction, and a unique-key
    // violation has to be caught OUTSIDE it (inside, the transaction would already be rollback-only).
    public List<EntradaEmitidaDTO> emitir(EmitirEntradasRequest req) {
        List<Entrada> previas = repo.findByOrdenId(req.ordenId());
        if (!previas.isEmpty()) {
            return mapper.toEmitidaDtos(previas);                       // payment retry: same tickets, no new rows
        }

        for (ItemEntradaRequest it : req.items()) {
            if ((it.fila() == null) != (it.asiento() == null)) {
                throw new IllegalArgumentException("fila y asiento deben informarse juntos o ninguno");
            }
        }

        // Last line of defense. Orden should check the limit BEFORE charging (see the count endpoint).
        long actuales = repo.countByClienteIdAndEventoIdAndEstadoIn(req.clienteId(), req.eventoId(), ACTIVAS);
        if (actuales + req.items().size() > maxPorCliente) {
            throw new LimiteEntradasExcedidoException("Límite de entradas por cliente excedido para este evento");
        }

        List<Entrada> nuevas = new ArrayList<>();
        for (int i = 0; i < req.items().size(); i++) {
            ItemEntradaRequest it = req.items().get(i);
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
            return mapper.toEmitidaDtos(repo.saveAllAndFlush(nuevas));  // all-or-nothing
        } catch (DataIntegrityViolationException ex) {
            // Either a concurrent retry of this same order won, or another order already owns a seat
            List<Entrada> existentes = repo.findByOrdenId(req.ordenId());
            if (!existentes.isEmpty()) {
                return mapper.toEmitidaDtos(existentes);
            }
            throw new AsientoNoDisponibleException("Alguno de los lugares ya fue vendido");
        }
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public EntradaEmitidaDTO obtenerPorId(Long id) {
        return mapper.toEmitidaDto(buscarOFalla(id));
    }

    @Transactional(readOnly = true)
    public List<EntradaEmitidaDTO> buscar(Long clienteId, String eventoId) {
        if (clienteId == null && (eventoId == null || eventoId.isBlank())) {
            throw new IllegalArgumentException("Debe indicar clienteId o eventoId");
        }
        List<Entrada> r;
        if (clienteId != null && eventoId != null && !eventoId.isBlank()) {
            r = repo.findByClienteIdAndEventoId(clienteId, eventoId);
        } else if (clienteId != null) {
            r = repo.findByClienteId(clienteId);
        } else {
            r = repo.findByEventoId(eventoId);
        }
        return mapper.toEmitidaDtos(r);
    }

    @Transactional(readOnly = true)
    public long contarActivas(Long clienteId, String eventoId) {
        return repo.countByClienteIdAndEventoIdAndEstadoIn(clienteId, eventoId, ACTIVAS);
    }

    // ---------------------------------------------------------------- state changes (driven by Orden)

    @Transactional
    public EntradaDTO cambiarEstado(Long id, CambioEstadoRequest req) {
        Entrada e = buscarOFalla(id);
        aplicarCambio(e, req.estado(), req.motivo());
        return mapper.toDto(e);
    }

    // Event cancelled: every VALIDA ticket of the event goes to REINTEGRO. Returns them so Orden can refund and notify.
    @Transactional
    public List<EntradaEmitidaDTO> reintegroPorEvento(String eventoId) {
        List<Entrada> afectadas = repo.findByEventoIdAndEstado(eventoId, EstadoEntrada.VALIDA);
        afectadas.forEach(e -> aplicarCambio(e, EstadoEntrada.REINTEGRO, MotivoCambio.EVENTO_CANCELADO));
        return mapper.toEmitidaDtos(afectadas);
    }

    // Compensation: Orden undoes an issuance. Returns the tickets that changed.
    @Transactional
    public List<EntradaEmitidaDTO> reintegroPorOrden(Long ordenId) {
        List<Entrada> afectadas = repo.findByOrdenIdAndEstado(ordenId, EstadoEntrada.VALIDA);
        afectadas.forEach(e -> aplicarCambio(e, EstadoEntrada.REINTEGRO, MotivoCambio.ANULACION_ORDEN));
        return mapper.toEmitidaDtos(afectadas);
    }

    // ---------------------------------------------------------------- helpers

    private void aplicarCambio(Entrada e, EstadoEntrada nuevo, MotivoCambio motivo) {
        if (e.getEstado() == nuevo) {
            return;                                              // idempotent: Orden may retry
        }
        if (!e.getEstado().puedePasarA(nuevo)) {
            throw new IllegalStateException("Transición no permitida: " + e.getEstado() + " -> " + nuevo);
        }
        if (nuevo == EstadoEntrada.REINTEGRO) {
            if (motivo == null) {
                throw new IllegalArgumentException("El motivo es obligatorio para pasar a REINTEGRO");
            }
            if (motivo == MotivoCambio.DEVOLUCION
                    && LocalDateTime.now().isAfter(e.getFechaEvento().minusHours(HORAS_LIMITE_DEVOLUCION))) {
                throw new DevolucionFueraDePlazoException("La devolución debe solicitarse hasta 72 horas antes del evento");
            }
            //si no pasaron 72 hs devolver la teca y notificar
        }
        e.cambiarEstado(nuevo);
        //actualizar la entrada
    }

    private Entrada buscarOFalla(Long id) {
        //controlar que la entrada pertenece al cliente
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));
    }

    public void revisarSolicitud (RevisionEntradaRequestDTO revisionDTO, Long idUsuario) {
        ResponseEntity<UsuarioResponseDTO> response = usuarioFeignClient.getById(idUsuario);
        UsuarioResponseDTO usuario = response.getBody();
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        boolean esAdmin = (usuario.getRol() == Rol.ADMINISTRADOR);
        if(!esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para revisar la entrada");
        }

        if(revisionDTO.getEstado() == EstadoEvento.APROBADO) {
            if (revisionDTO.getIdEntrada() == null) {
                throw new IllegalArgumentException("Para aprobar una devolución hay que indicar idEntrada");
            }
            // 1. Anular la entrada PRIMERO: controla las 72 h y libera el asiento.
            //    Si está fuera de plazo lanza DevolucionFueraDePlazoException (422) y no se reembolsa nada.
            Entrada entrada = buscarOFalla(revisionDTO.getIdEntrada());
            aplicarCambio(entrada, EstadoEntrada.REINTEGRO, MotivoCambio.DEVOLUCION);
            repo.save(entrada);   // este método no es @Transactional: hay que guardar explícitamente
            // 2. Reintegrar el dinero y 3. liberar la reserva (queda PENDIENTE vencida y el job la pasa a VENCIDA)
            pagoFeignClient.reembolsarPago(revisionDTO.getIdReserva());
            reservaFeignClient.actualizarEstadoReserva(revisionDTO.getIdReserva(), "PENDIENTE");
        }
    }
}