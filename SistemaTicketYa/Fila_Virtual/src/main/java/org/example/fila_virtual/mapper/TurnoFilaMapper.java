package org.example.fila_virtual.mapper;


import org.example.fila_virtual.DTO.Response.TurnoResponseDTO;
import org.example.fila_virtual.entity.TurnoFila;
import org.example.fila_virtual.utils.EstadoTurno;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TurnoFilaMapper {

    public TurnoFila convertToEntity(Long clienteId, String eventoId) {
        TurnoFila turno = new TurnoFila();
        turno.setClienteId(clienteId);
        turno.setEventoId(eventoId);
        turno.setEstado(EstadoTurno.EN_ESPERA);
        turno.setFechaIngreso(LocalDateTime.now());
        return turno;
    }

    public TurnoResponseDTO convertToDTO(TurnoFila turno, long posicionActual, long personasEnEspera) {
        boolean habilitado = turno.getEstado() == EstadoTurno.HABILITADO;

        String mensaje = habilitado
                ? "¡Es tu turno! Tenés habilitada la compra hasta " + turno.getFechaExpiracion()
                : "Estás en la posición " + posicionActual + " de la fila virtual.";

        return TurnoResponseDTO.builder()
                .turnoId(turno.getId())
                .eventoId(turno.getEventoId())
                .clienteId(turno.getClienteId())
                .estado(turno.getEstado())
                .posicionActual(habilitado ? 0L : posicionActual)
                .personasEnEspera(personasEnEspera)
                .tokenAcceso(habilitado ? turno.getTokenAcceso() : null)
                .fechaIngreso(turno.getFechaIngreso())
                .fechaExpiracion(habilitado ? turno.getFechaExpiracion() : null)
                .mensaje(mensaje)
                .build();
    }
}
