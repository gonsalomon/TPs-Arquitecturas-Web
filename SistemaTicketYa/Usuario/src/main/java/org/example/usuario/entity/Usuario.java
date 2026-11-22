package org.example.usuario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column
    private String nombre;

    @Column
    private String apellido;

    @Column
    @Email
    private String email;

    @Column
    private String ciudad;

    @ElementCollection
    @CollectionTable(
            name = "usuario_artistas_favoritos",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "artista")
    private Set<String> artistasFavoritos;

    @Column( nullable = false )
    private String password;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_authority",
            joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "idUsuario") },
            inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    private List<Authority> authorities;

    public Usuario(String nombre, String apellido, String email, String ciudad, List<Authority> authorities,
                   String password, Set<String> artistasFavoritos ) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.ciudad = ciudad;
        this.authorities = authorities;
        this.password = password;
        this.artistasFavoritos = artistasFavoritos;
    }
}
