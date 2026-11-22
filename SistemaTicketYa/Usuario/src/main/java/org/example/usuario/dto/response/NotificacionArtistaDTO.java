package org.example.usuario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificacionArtistaDTO {
    private Long idUsuario;
    private String email;
}
