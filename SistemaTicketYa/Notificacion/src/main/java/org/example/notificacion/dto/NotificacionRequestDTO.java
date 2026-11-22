package org.example.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.notificacion.utils.NotificacionCanal;
import org.example.notificacion.utils.NotificacionTipo;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private NotificacionTipo tipo;
    private NotificacionCanal canal;
    private String email;
    private LocalDateTime fechaAnterior;
    private LocalDateTime fechaNueva;
}

