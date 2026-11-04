package org.example.reserva.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.reserva.utils.EstadoReserva;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @Column
    private Long idCliente;

    @Column
    private String idEvento;

    @Column
    private String sector;

    @Column
    private Float monto;

    @ElementCollection
    @CollectionTable(name = "reserva_asientos", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column
    private List<Long> asientos;

    @Enumerated(EnumType.STRING)
    @Column
    private EstadoReserva estado;

    @Column
    private LocalDateTime fecha_inicio;

    @Column
    private LocalDateTime fecha_vencimiento;

    public Reserva(Long idCliente, String idEvento, String sector, List<Long> asientos){
        this.idCliente = idCliente;
        this.idEvento = idEvento;
        this.sector = sector;
        this.estado = EstadoReserva.PENDIENTE;
        this.fecha_inicio = LocalDateTime.now();
        this.fecha_vencimiento = fecha_inicio.plusMinutes(10);
        this.asientos = asientos;
    }

}
