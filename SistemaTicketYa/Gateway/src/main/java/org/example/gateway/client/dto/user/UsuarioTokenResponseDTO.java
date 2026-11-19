package org.example.gateway.client.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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