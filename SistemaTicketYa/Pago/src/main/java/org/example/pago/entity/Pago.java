package org.example.pago.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.pago.utils.Estado;
import org.example.pago.utils.MetodoPago;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column
    private MetodoPago metodoPago;

    @Column
    private Float monto;

    @Column
    private LocalDateTime fecha_emision;

    public Pago(Long reserva, Long idCliente, MetodoPago metodoPago){
        this.idReserva = reserva;
        this.idCliente = idCliente;
        this.metodoPago = metodoPago;
    }
}
