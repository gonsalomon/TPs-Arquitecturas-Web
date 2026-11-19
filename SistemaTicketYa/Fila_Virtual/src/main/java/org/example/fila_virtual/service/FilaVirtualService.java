package org.example.fila_virtual.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fila_virtual.feignClient.EventoClient;
import org.example.fila_virtual.DTO.Request.EventoExternoDTO;
import org.example.fila_virtual.DTO.Response.TurnoResponseDTO;
import org.example.fila_virtual.DTO.Request.ValidacionAccesoDTO;
import org.example.fila_virtual.mapper.TurnoFilaMapper;
import org.example.fila_virtual.utils.EstadoTurno;
import org.example.fila_virtual.entity.TurnoFila;
import org.example.fila_virtual.repository.TurnoFilaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilaVirtualService {

    private final TurnoFilaRepository repository;
    private final EventoClient eventoClient;
    private final TurnoFilaMapper mapper;

    @Value("${fila.tanda.tamano:2}")
    private int tamanoTanda;

    @Value("${fila.turno.duracion-minutos:5}")
    private int duracionTurnoMinutos;

    @Value("${fila.validar-evento-externo:true}")
    private boolean validarEventoExterno;

    private static final List<EstadoTurno> ESTADOS_ACTIVOS =
            List.of(EstadoTurno.EN_ESPERA, EstadoTurno.HABILITADO);

    @Transactional
    public TurnoResponseDTO ingresar(Long clienteId, String eventoId) {
        if (validarEventoExterno) {
            validarEventoAltaDemanda(eventoId);
        }

        // Idempotencia: si ya está en la fila o habilitado, devolvemos su turno actual
        Optional<TurnoFila> existente = repository
                .findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
                        eventoId, clienteId, ESTADOS_ACTIVOS);

        if (existente.isPresent()) {
            return construirRespuesta(existente.get());
        }

        TurnoFila nuevoTurno = TurnoFila.builder()
                .eventoId(eventoId)
                .clienteId(clienteId)
                .estado(EstadoTurno.EN_ESPERA)
                .fechaIngreso(LocalDateTime.now())
                .build();

        TurnoFila guardado = repository.saveAndFlush(nuevoTurno);

        // Si hay cupo libre en la tanda actual y no había nadie esperando adelante, lo habilita en el acto
        avanzarTandaEvento(eventoId);

        // Recargamos por si fue promovido inmediatamente a HABILITADO
        TurnoFila actualizado = repository.findById(guardado.getId()).orElse(guardado);
        return construirRespuesta(actualizado);
    }

    @Transactional(readOnly = true)
    public TurnoResponseDTO consultarPosicion(Long clienteId, String eventoId) {
        TurnoFila turno = repository
                .findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
                        eventoId, clienteId, ESTADOS_ACTIVOS)
                .orElseThrow(() -> new IllegalStateException(
                        "No tenés un turno activo en la fila para este evento."));

        return construirRespuesta(turno);
    }

    @Transactional(readOnly = true)
    public ValidacionAccesoDTO validarAcceso(Long clienteId, String eventoId, String tokenAcceso) {
        Optional<TurnoFila> turnoOpt = repository
                .findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
                        eventoId, clienteId, List.of(EstadoTurno.HABILITADO));

        if (turnoOpt.isEmpty()) {
            return new ValidacionAccesoDTO(false, "El cliente no está habilitado para comprar en este evento.");
        }

        TurnoFila turno = turnoOpt.get();
        if (turno.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return new ValidacionAccesoDTO(false, "El turno para comprar ha expirado.");
        }

        if (tokenAcceso != null && !tokenAcceso.equals(turno.getTokenAcceso())) {
            return new ValidacionAccesoDTO(false, "Token de acceso inválido.");
        }

        return new ValidacionAccesoDTO(true, "Acceso habilitado.");
    }

    @Transactional
    public void marcarCompletado(Long clienteId, String eventoId) {
        TurnoFila turno = repository
                .findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
                        eventoId, clienteId, List.of(EstadoTurno.HABILITADO))
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un turno HABILITADO para completar."));

        turno.setEstado(EstadoTurno.COMPLETADO);
        repository.save(turno);

        // Al liberar un cupo, habilitamos de inmediato al siguiente en la fila
        avanzarTandaEvento(eventoId);
    }

    @Transactional
    public void abandonarFila(Long clienteId, String eventoId) {
        TurnoFila turno = repository
                .findFirstByEventoIdAndClienteIdAndEstadoInOrderByFechaIngresoDesc(
                        eventoId, clienteId, ESTADOS_ACTIVOS)
                .orElseThrow(() -> new IllegalStateException(
                        "No tenés un turno activo para cancelar."));

        boolean estabaHabilitado = turno.getEstado() == EstadoTurno.HABILITADO;
        turno.setEstado(EstadoTurno.CANCELADO);
        repository.save(turno);

        if (estabaHabilitado) {
            avanzarTandaEvento(eventoId);
        }
    }

    /**
     * Proceso automático: expira los turnos vencidos y habilita nuevas tandas en orden FIFO.
     */
    @Scheduled(fixedDelayString = "${fila.scheduler.intervalo-ms:10000}")
    @Transactional
    public void procesarTandasAutomaticamente() {
        // 1. Expirar turnos vencidos
        List<TurnoFila> vencidos = repository.findByEstadoAndFechaExpiracionBefore(
                EstadoTurno.HABILITADO, LocalDateTime.now());

        for (TurnoFila turno : vencidos) {
            turno.setEstado(EstadoTurno.EXPIRADO);
            log.info("Turno {} del cliente {} para evento {} EXPIRADO.",
                    turno.getId(), turno.getClienteId(), turno.getEventoId());
        }
        if (!vencidos.isEmpty()) {
            repository.saveAll(vencidos);
        }

        // 2. Avanzar tandas en todos los eventos con clientes en espera
        List<String> eventosEnEspera = repository.findEventosConEstado(EstadoTurno.EN_ESPERA);
        for (String eventoId : eventosEnEspera) {
            avanzarTandaEvento(eventoId);
        }
    }

    @Transactional
    public int avanzarTandaEvento(String eventoId) {
        long habilitadosActuales = repository.countByEventoIdAndEstado(eventoId, EstadoTurno.HABILITADO);
        int cuposDisponibles = (int) (tamanoTanda - habilitadosActuales);

        if (cuposDisponibles <= 0) {
            return 0;
        }

        List<TurnoFila> siguientes = repository.findByEventoIdAndEstadoOrderByFechaIngresoAscIdAsc(
                eventoId, EstadoTurno.EN_ESPERA, PageRequest.of(0, cuposDisponibles));

        LocalDateTime ahora = LocalDateTime.now();
        for (TurnoFila turno : siguientes) {
            turno.setEstado(EstadoTurno.HABILITADO);
            turno.setTokenAcceso(UUID.randomUUID().toString());
            turno.setFechaHabilitacion(ahora);
            turno.setFechaExpiracion(ahora.plusMinutes(duracionTurnoMinutos));
            log.info("Cliente {} HABILITADO para evento {} (expira: {})",
                    turno.getClienteId(), eventoId, turno.getFechaExpiracion());
        }

        repository.saveAll(siguientes);
        return siguientes.size();
    }

    private void validarEventoAltaDemanda(String eventoId) {
        try {
            EventoExternoDTO evento = eventoClient.obtenerEvento(eventoId);
            if (!"PUBLICADO".equalsIgnoreCase(evento.getEstado())) {
                throw new IllegalArgumentException("El evento no se encuentra publicado para la venta.");
            }
            if (!Boolean.TRUE.equals(evento.getAltaDemanda())) {
                throw new IllegalArgumentException("Este evento no requiere fila virtual (no es de alta demanda).");
            }
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("El evento " + eventoId + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo verificar el evento en el microservicio de Eventos.", e);
        }
    }

    private TurnoResponseDTO construirRespuesta(TurnoFila turno) {
        long totalEnEspera = repository.countByEventoIdAndEstado(turno.getEventoId(), EstadoTurno.EN_ESPERA);

        long posicion = 0L;
        if (turno.getEstado() == EstadoTurno.EN_ESPERA) {
            long delante = repository.contarDelante(
                    turno.getEventoId(), EstadoTurno.EN_ESPERA, turno.getFechaIngreso(), turno.getId());
            posicion = delante + 1;
        }

        return mapper.convertToDTO(turno, posicion, totalEnEspera);
    }
}
