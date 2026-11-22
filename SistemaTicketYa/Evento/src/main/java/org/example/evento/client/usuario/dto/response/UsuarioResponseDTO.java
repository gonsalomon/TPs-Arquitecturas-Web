package org.example.evento.client.usuario.dto.response;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String ciudad;
    private Set<String> authorities;
}
