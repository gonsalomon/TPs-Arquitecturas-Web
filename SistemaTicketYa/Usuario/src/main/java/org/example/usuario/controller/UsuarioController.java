package org.example.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@AllArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de usuarios y sus servicios asociados")
public class UsuarioController {
    private final UsuarioService service;

    /*-------- ENDPOINTS CRUD ----------*/
    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Crea un usuario validando el contenido del request. Si los datos son inválidos devuelve un error."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario creado correctamente",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos / error en la creación",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("")
    public ResponseEntity<?> createUsuario(@RequestBody UsuarioRequestDTO usuario) {
        UsuarioResponseDTO nuevo = service.save(usuario);
        return ResponseEntity.ok(nuevo);
    }

}
