public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    List<Entrada> findByOrdenId(Long ordenId);

    long countByClienteIdAndEventoIdAndEstadoIn(Long clienteId, Long eventoId, Collection<EstadoEntrada> estados);

    // Bulk JPQL bypasses @PreUpdate, so claveAsientoActivo MUST be nulled here or seats stay locked.
    @Modifying(clearAutomatically = true)
    @Query("""
        update Entrada e
           set e.estado = :nuevo, e.claveAsientoActivo = null
         where e.eventoId = :eventoId and e.estado in :origen
        """)
    int cambiarEstadoPorEvento(@Param("eventoId") Long eventoId,
                               @Param("nuevo") EstadoEntrada nuevo,
                               @Param("origen") Collection<EstadoEntrada> origen);
    List<Entrada> findByClienteId(Long clienteId);
    List<Entrada> findByEventoId(Long eventoId);
    List<Entrada> findByClienteIdAndEventoId(Long clienteId, Long eventoId);
}