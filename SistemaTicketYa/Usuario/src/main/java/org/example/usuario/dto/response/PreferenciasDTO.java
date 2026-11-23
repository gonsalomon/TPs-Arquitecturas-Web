package org.example.usuario.dto.response;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciasDTO {
    private String ciudad;

    @Builder.Default
    private Set<String> tiposEvento = new HashSet<>();

    @Builder.Default
    private Set<String> artistasFavoritos = new HashSet<>();
}
