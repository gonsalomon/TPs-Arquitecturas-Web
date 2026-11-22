package org.example.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.usuario.dto.response.UsuarioTokenResponseDTO;
import org.example.usuario.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Endpoints de autenticación y obtención de usuarios")
public class AuthController {
    private final UsuarioService usuarioService;

    @Operation(
            summary = "Obtener usuario por email",
            description = "Devuelve la información del usuario y sus autoridades, usada para autenticación JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @GetMapping("/{email}")
    public ResponseEntity<UsuarioTokenResponseDTO> getUserByUsername(@PathVariable(name= "email") String email) {
        UsuarioTokenResponseDTO usuario = usuarioService.findOneWithAuthoritiesByEmailIgnoreCase(email);
        return ResponseEntity.ok(usuario);
    }
}