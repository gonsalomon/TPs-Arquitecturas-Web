package org.example.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.dto.response.UsuarioTokenResponseDTO;
import org.example.usuario.entity.Authority;
import org.example.usuario.entity.Usuario;
import org.example.usuario.mapper.UsuarioMapper;
import org.example.usuario.repository.AuthorityRepository;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    
    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    @Transactional
    public UsuarioResponseDTO save(UsuarioRequestDTO request) {

        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese email en el sistema");
        });
        //encripto el password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);
        //obtengo las autoridades desde el request y las busco en la DB
        List<String> authorityNames = request.getAuthorities();
        List<Authority> authorities = authorityRepository.findAllById(authorityNames);
        if (authorities.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron authorities para: " + authorityNames);
        }

        Usuario usuario = mapper.convertToEntity(request);
        usuario.setAuthorities(new ArrayList<>(authorities));
        Usuario nuevo = repository.save(usuario);
        return mapper.convertToDTO(nuevo);
    }

    @Transactional
    public List<UsuarioResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::convertToDTO)
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO findByIdUsuario(Long idUsuario) {
        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario con el idUsuario: " + idUsuario));
        return mapper.convertToDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO update(Long idUsuario, UsuarioRequestDTO request) {
        Usuario usuarioEditado = repository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario con el id: " + idUsuario));
        usuarioEditado.setNombre(request.getNombre());
        usuarioEditado.setApellido(request.getApellido());
        usuarioEditado.setEmail(request.getEmail());
        usuarioEditado.setCiudad(request.getCiudad());
        repository.save(usuarioEditado);
        return mapper.convertToDTO(usuarioEditado);
    }

    @Transactional
    public void delete(Long idUsuario) {
        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un usuario con el id: " + idUsuario));
        repository.delete(usuario);
    }

    public UsuarioTokenResponseDTO findOneWithAuthoritiesByEmailIgnoreCase(String email) {
        // Busco el usuario con el mail que viene por param (devuelve un opcional de usuario con su lista de roles)
        Usuario usuario = repository.findOneWithAuthoritiesByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return UsuarioMapper.convertToUsuarioTokenResponse(usuario);
    }
}
