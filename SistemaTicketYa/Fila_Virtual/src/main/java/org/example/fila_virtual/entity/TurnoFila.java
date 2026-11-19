package org.example.fila_virtual.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fila_virtual.utils.EstadoTurno;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
public class TurnoFila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private String eventoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    @Column(name = "token_acceso", unique = true)
    private String tokenAcceso;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_habilitacion")
    private LocalDateTime fechaHabilitacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
}
