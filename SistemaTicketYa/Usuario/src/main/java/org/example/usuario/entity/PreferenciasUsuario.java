package org.example.usuario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preferencias_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciasUsuario {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 100)
    private String ciudad;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "preferencias_tipos_evento",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "tipo_evento", nullable = false)
    @Builder.Default
    private Set<String> tiposEvento = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "preferencias_artistas",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "artista", nullable = false)
    @Builder.Default
    private Set<String> artistasFavoritos = new HashSet<>();
}
