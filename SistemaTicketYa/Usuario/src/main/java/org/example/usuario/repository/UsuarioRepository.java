package org.example.usuario.repository;

import jakarta.validation.constraints.NotNull;
import org.example.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("UsuarioRepository")
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(@NotNull(message = "El email es obligatorio")String email);

    @Query("""
        FROM Usuario u JOIN FETCH u.authorities
        WHERE lower(u.email) =  ?1
    """)
    Optional<Usuario> findOneWithAuthoritiesByEmailIgnoreCase( String email );
}
