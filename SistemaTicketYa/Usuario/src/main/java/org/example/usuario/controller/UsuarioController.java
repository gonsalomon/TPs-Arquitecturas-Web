package org.example.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Operation(summary = "Obtener todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping("")
    public ResponseEntity<List<UsuarioResponseDTO>> getAll(){
        List<UsuarioResponseDTO> usuarios = service.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Obtener un usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponseDTO> getById(
            @Parameter(description = "ID del usuario", example = "12")
            @PathVariable Long idUsuario){
        UsuarioResponseDTO usuario = service.findByIdUsuario(idUsuario);
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Actualizar un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "ID del usuario", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuario) {
        UsuarioResponseDTO usuarioEditado = service.update(id, usuario);
        return ResponseEntity.ok(usuarioEditado);
    }

    @Operation(summary = "Eliminar un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado con éxito"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> delete(
            @Parameter(description = "ID del usuario") @PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
