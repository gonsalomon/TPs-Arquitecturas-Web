package org.example.usuario.service;

import lombok.RequiredArgsConstructor;
import org.example.usuario.dto.response.PreferenciasDTO;
import org.example.usuario.entity.PreferenciasUsuario;
import org.example.usuario.repository.PreferenciasUsuarioRepository;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenciasService {

    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PreferenciasDTO guardarOActualizar(Long usuarioId, PreferenciasDTO dto) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + usuarioId);
        }

        PreferenciasUsuario entidad = preferenciasRepository.findById(usuarioId)
                .orElseGet(() -> PreferenciasUsuario.builder().usuarioId(usuarioId).build());

        entidad.setCiudad(dto.getCiudad() != null ? dto.getCiudad().trim() : null);
        entidad.setTiposEvento(limpiarSet(dto.getTiposEvento()));
        entidad.setArtistasFavoritos(limpiarSet(dto.getArtistasFavoritos()));

        return toDTO(preferenciasRepository.save(entidad));
    }

    @Transactional(readOnly = true)
    public PreferenciasDTO obtenerPorUsuario(Long usuarioId) {
        return preferenciasRepository.findById(usuarioId)
                .map(this::toDTO)
                .orElseGet(() -> PreferenciasDTO.builder().build());
    }

    private Set<String> limpiarSet(Set<String> valores) {
        if (valores == null) return new HashSet<>();
        return valores.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private PreferenciasDTO toDTO(PreferenciasUsuario entidad) {
        return PreferenciasDTO.builder()
                .ciudad(entidad.getCiudad())
                .tiposEvento(new HashSet<>(entidad.getTiposEvento()))
                .artistasFavoritos(new HashSet<>(entidad.getArtistasFavoritos()))
                .build();
    }
}