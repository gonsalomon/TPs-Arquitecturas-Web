package org.example.usuario.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequestDTO {
    @NotNull(message = "El nombre es obligatorio")
    private String nombre;
    @NotNull(message = "El apellido es obligatorio")
    private String apellido;
    @NotNull(message = "El email es obligatorio")
    private String email;
    @NotNull(message = "La ciudad es obligatoria")
    private String ciudad;
    @NotNull( message = "La contraseña es un campo requerido." )
    @NotEmpty( message = "La contraseña es un campo requerido." )
    private String password;

    @NotNull( message = "Los roles son un campo requerido." )
    @NotEmpty( message = "Los roles son un campo requerido." )
    private List<String> authorities;
}
