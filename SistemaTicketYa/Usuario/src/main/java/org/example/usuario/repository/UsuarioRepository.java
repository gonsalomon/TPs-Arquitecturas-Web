package org.example.usuario.repository;

import feign.Param;
import jakarta.validation.constraints.NotNull;
import org.example.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("UsuarioRepository")
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(@NotNull(message = "El email es obligatorio")String email);

    @Query("""
        FROM Usuario u JOIN FETCH u.authorities
        WHERE lower(u.email) =  ?1
    """)
    Optional<Usuario> findOneWithAuthoritiesByEmailIgnoreCase( String email );

    @Query("""
        SELECT DISTINCT u
        FROM Usuario u
        JOIN u.artistasFavoritos a
        WHERE LOWER(u.ciudad) = LOWER(:ciudad)
        AND LOWER(a) IN :artistas
    """)
    List<Usuario> buscarUsuariosPorArtistasYCiudad(
            @Param("artistas") Set<String> artistas,
            @Param("ciudad") String ciudad
    );
}
