package org.example.orden.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orden.utils.EstadoOrden;
import org.example.orden.utils.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orden", indexes = {
        @Index(name = "idx_orden_cliente_evento_estado", columnList = "cliente_id, evento_id, estado"),
        @Index(name = "idx_orden_estado_vencimiento", columnList = "estado, fecha_vencimiento")
})
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "evento_id", nullable = false)
    private String eventoId;

    @Column(nullable = false)
    private String sector;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago")
    private MedioPago medioPago;

    @Column(name = "id_reserva")
    private Long idReserva;

    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Version
    private Long version;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemOrden> items = new ArrayList<>();

    public Orden(Long clienteId, String eventoId, String sector, LocalDateTime fechaEvento) {
        this.clienteId = clienteId;
        this.eventoId = eventoId;
        this.sector = sector;
        this.fechaEvento = fechaEvento;
        this.estado = EstadoOrden.PENDIENTE_PAGO;
        this.total = BigDecimal.ZERO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void agregarItem(ItemOrden item) {
        item.setOrden(this);
        this.items.add(item);
        this.total = this.total.add(item.getPrecioUnitario());
    }

    public void liberarClaves() {
        items.forEach(ItemOrden::liberarClaveAsientoActivo);
    }

    public void cambiarEstado(EstadoOrden nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
    }
}
