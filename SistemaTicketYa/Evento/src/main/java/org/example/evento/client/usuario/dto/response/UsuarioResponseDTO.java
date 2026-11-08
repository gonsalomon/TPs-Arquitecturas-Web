package org.example.evento.client.usuario.dto.response;

import lombok.*;
import org.example.evento.client.utils.Rol;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
}
