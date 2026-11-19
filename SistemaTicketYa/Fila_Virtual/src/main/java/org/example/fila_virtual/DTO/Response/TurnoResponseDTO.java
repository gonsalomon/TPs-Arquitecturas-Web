package org.example.fila_virtual.DTO.Response;

import lombok.Builder;
import lombok.Data;
import org.example.fila_virtual.utils.EstadoTurno;

import java.time.LocalDateTime;

@Data
@Builder
public class TurnoResponseDTO {
    private Long turnoId;
    private String eventoId;
    private Long clienteId;
    private EstadoTurno estado;
    private Long posicionActual;     // 0 si está habilitado; >= 1 si está en espera
    private Long personasEnEspera;
    private String tokenAcceso;      // Solo se informa cuando estado == HABILITADO
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaExpiracion;
    private String mensaje;
}
