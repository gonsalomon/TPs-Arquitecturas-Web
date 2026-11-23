package org.example.evento.dto.request;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciasExternasDTO {
    private String ciudad;
    private Set<String> tiposEvento = new HashSet<>();
    private Set<String> artistasFavoritos = new HashSet<>();
}
