package org.example.entrada.client.usuario.response.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entrada.client.utils.Rol;

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
