package org.example.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.entity.Usuario;
import org.example.usuario.mapper.UsuarioMapper;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    @Transactional
    public UsuarioResponseDTO save(UsuarioRequestDTO request) {
        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese email en el sistema");
        });
        Usuario usuario = mapper.convertToEntity(request);
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
        usuarioEditado.setRol(request.getRol());
        repository.save(usuarioEditado);
        return mapper.convertToDTO(usuarioEditado);
    }

    @Transactional
    public void delete(Long idUsuario) {
        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un usuario con el id: " + idUsuario));
        repository.delete(usuario);
    }
}
