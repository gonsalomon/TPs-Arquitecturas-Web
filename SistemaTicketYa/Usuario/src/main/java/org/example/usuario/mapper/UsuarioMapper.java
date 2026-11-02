package org.example.usuario.mapper;

import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO convertToDTO(Usuario usuario){
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }

    public Usuario convertToEntity(UsuarioRequestDTO usuarioRequestDTO){
        return new Usuario(
                usuarioRequestDTO.getNombre(),
                usuarioRequestDTO.getApellido(),
                usuarioRequestDTO.getEmail(),
                usuarioRequestDTO.getRol()
        );
    }
}
