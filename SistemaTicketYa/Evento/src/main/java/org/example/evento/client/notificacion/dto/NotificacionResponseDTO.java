package org.example.evento.client.notificacion.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class NotificacionResponseDTO {
    private Long id;
    private String tipo;
    private String canal;
    private String email;
    private String estado;
}
