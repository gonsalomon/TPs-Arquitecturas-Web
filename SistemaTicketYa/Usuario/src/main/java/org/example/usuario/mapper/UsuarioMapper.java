package org.example.usuario.mapper;

import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.AuthorityResponseDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.dto.response.UsuarioTokenResponseDTO;
import org.example.usuario.entity.Authority;
import org.example.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO convertToDTO(Usuario usuario){
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getCiudad(),
                usuario.getAuthorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toSet()),
                usuario.getArtistasFavoritos()
        );
    }

    public Usuario convertToEntity(UsuarioRequestDTO usuarioRequestDTO){
        List<Authority> authorities = new ArrayList<>();
        usuarioRequestDTO.getAuthorities().stream().map(Authority::new).forEach(authorities::add);
        return new Usuario(
                usuarioRequestDTO.getNombre(),
                usuarioRequestDTO.getApellido(),
                usuarioRequestDTO.getEmail(),
                usuarioRequestDTO.getCiudad(),
                authorities,
                usuarioRequestDTO.getPassword(),
                usuarioRequestDTO.getArtistasFavoritos()
        );
    }
    public static UsuarioTokenResponseDTO convertToUsuarioTokenResponse(Usuario usuario) {
        List<AuthorityResponseDTO> authoritiesDTO = new ArrayList<>();
        for(Authority authority : usuario.getAuthorities()) {
            authoritiesDTO.add(new AuthorityResponseDTO(authority.getName()));
        }
        return new UsuarioTokenResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getPassword(),
                authoritiesDTO
        );
    }
}
