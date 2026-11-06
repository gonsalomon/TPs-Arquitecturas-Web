package org.example.orden.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "item_orden")
public class ItemOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private Orden orden;

    @Column
    private Integer fila;

    @Column
    private Integer asiento;

    @Column(name = "numero_asiento")
    private Integer numeroAsiento;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "clave_asiento_activo", unique = true)
    private String claveAsientoActivo;

    public ItemOrden(Integer fila, Integer asiento, Integer numeroAsiento, BigDecimal precioUnitario) {
        this.fila = fila;
        this.asiento = asiento;
        this.numeroAsiento = numeroAsiento;
        this.precioUnitario = precioUnitario;
    }

    public void liberarClaveAsientoActivo() {
        this.claveAsientoActivo = null;
    }

    @PrePersist
    @PreUpdate
    private void sincronizarClaveAsientoActivo() {
        if (orden == null || fila == null || asiento == null
                || orden.getEstado() == null || !orden.getEstado().esActiva()) {
            this.claveAsientoActivo = null;
            return;
        }
        this.claveAsientoActivo = orden.getEventoId() + ":" + orden.getSector() + ":" + fila + ":" + asiento;
    }
}
