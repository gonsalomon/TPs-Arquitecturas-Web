package org.example.notificacion.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.notificacion.utils.NotificacionCanal;
import org.example.notificacion.utils.NotificacionTipo;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionResponseDTO {
    private Long id;
    private NotificacionTipo tipo;
    private NotificacionCanal canal;
    private String email;
    private String estado;

}