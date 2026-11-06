package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemEmitirDTO {
    private String sector;
    private Integer fila;
    private Integer asiento;
    private BigDecimal precio;
}
