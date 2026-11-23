package org.example.usuario.repository;

import org.example.usuario.entity.PreferenciasUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenciasUsuarioRepository extends JpaRepository<PreferenciasUsuario, Long> {
}
