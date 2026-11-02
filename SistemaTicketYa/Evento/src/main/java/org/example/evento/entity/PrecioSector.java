package org.example.evento.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Precio que define el organizador para un sector del lugar en este evento. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrecioSector {
    private String sector;
    private Double precio;
}
