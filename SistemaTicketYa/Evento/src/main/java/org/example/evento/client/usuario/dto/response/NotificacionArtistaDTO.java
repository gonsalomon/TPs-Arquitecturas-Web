package org.example.evento.client.usuario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionArtistaDTO {
    private Long idUsuario;
    private String email;
}
