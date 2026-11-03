@Entity
@Table(name = "entrada", uniqueConstraints = {
        @UniqueConstraint(name = "uk_entrada_asiento_activo", columnNames = "clave_asiento_activo"),
        @UniqueConstraint(name = "uk_entrada_idempotencia", columnNames = "clave_idempotencia"),
        @UniqueConstraint(name = "uk_entrada_qr", columnNames = "codigo_qr")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Entrada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventoId;
    private Long sectorId;
    private Long clienteId;
    private Long ordenId;
    private Integer fila;       // null if sector is not numbered
    private Integer asiento;    // null if sector is not numbered
    private BigDecimal precio;
    private LocalDateTime fechaEvento; // snapshot, needed for the 72h refund rule

    @Enumerated(EnumType.STRING)
    private EstadoEntrada estado = EstadoEntrada.VALIDA;

    @Column(name = "codigo_qr", nullable = false)
    private String codigoQr = UUID.randomUUID().toString();

    // ordenId + "-" + item index. Makes payment retries idempotent.
    @Column(name = "clave_idempotencia", nullable = false)
    private String claveIdempotencia;

    // "evento:sector:fila:asiento" while the seat is occupied, NULL otherwise.
    // Multiple NULLs are allowed in unique columns (Postgres/MySQL), so freed seats can be resold.
    @Column(name = "clave_asiento_activo")
    private String claveAsientoActivo;

    @Version
    private Long version;

    @PrePersist @PreUpdate
    void sincronizarClave() {
        claveAsientoActivo = (estado.ocupaLugar() && asiento != null)
                ? eventoId + ":" + sectorId + ":" + fila + ":" + asiento
                : null;
    }
    // getters/setters
}