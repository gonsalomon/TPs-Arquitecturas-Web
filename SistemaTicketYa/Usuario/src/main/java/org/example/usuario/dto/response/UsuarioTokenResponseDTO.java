package org.example.usuario.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class UsuarioTokenResponseDTO {
    private Long id;
    private String usuario;
    private String password;
    private List<AuthorityResponseDTO> roles;
}
