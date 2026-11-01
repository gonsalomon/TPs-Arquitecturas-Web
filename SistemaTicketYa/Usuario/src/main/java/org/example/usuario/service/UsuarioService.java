package org.example.usuario.service;

import lombok.AllArgsConstructor;
import org.example.usuario.dto.request.UsuarioRequestDTO;
import org.example.usuario.dto.response.UsuarioResponseDTO;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    /*-------------------------- MÉTODOS PARA EL CRUD --------------------------*/
    @Transactional
    public UsuarioResponseDTO save(UsuarioRequestDTO request) {
        return new  UsuarioResponseDTO();
    }
}
