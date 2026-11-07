package org.example.entrada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.entrada.utils.EstadoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "entrada", indexes = {
        @Index(name = "idx_entrada_evento", columnList = "evento_id"),
        @Index(name = "idx_entrada_cliente_evento", columnList = "cliente_id, evento_id"),
        @Index(name = "idx_entrada_orden", columnList = "orden_id")
})
@Getter
@Setter
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // eventoId and sectorId belong to Evento (MongoDB), hence String
    @Column(name = "evento_id", nullable = false)
    private String eventoId;

    @Column(name = "sector_id", nullable = false)
    private String sectorId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    // null for non-numbered sectors (zones)
    @Column(name = "fila")
    private Integer fila;

    @Column(name = "asiento")
    private Integer asiento;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    // snapshot sent by Orden at issuance; needed for the 72h refund rule
    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private EstadoEntrada estado = EstadoEntrada.VALIDA;

    @Column(name = "codigo_qr", nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private String codigoQr = UUID.randomUUID().toString();

    // ordenId + "-" + item index: makes a payment retry idempotent
    @Column(name = "clave_idempotencia", nullable = false, unique = true)
    private String claveIdempotencia;

    // "eventoId:sectorId:fila:asiento" while the seat is occupied, NULL otherwise (or for zones).
    // MySQL/Postgres allow many NULLs in a unique column, so a freed seat can be sold again.
    @Column(name = "clave_asiento_activo", unique = true)
    @Setter(AccessLevel.NONE)
    private String claveAsientoActivo;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    @PrePersist
    void antesDeInsertar() {
        recalcularClave();
    }

    // The only way to change the state: keeps claveAsientoActivo consistent
    public void cambiarEstado(EstadoEntrada nuevo) {
        this.estado = nuevo;
        recalcularClave();
    }

    private void recalcularClave() {
        this.claveAsientoActivo = (estado.ocupaLugar() && fila != null && asiento != null)
                ? eventoId + ":" + sectorId + ":" + fila + ":" + asiento
                : null;
    }
}