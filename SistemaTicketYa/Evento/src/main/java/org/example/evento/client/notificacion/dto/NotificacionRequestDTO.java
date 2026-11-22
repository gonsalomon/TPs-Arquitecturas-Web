package org.example.evento.client.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private String tipo;
    private String canal;
    private String email;
    private LocalDateTime fechaAnterior;
    private LocalDateTime fechaNueva;
}
