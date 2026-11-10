package org.example.entrada.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntradaEmitidaDTO {
    private Long id;
    private String codigoQr;
    private String sector;
    private Integer fila;
    private Integer asiento;
}
