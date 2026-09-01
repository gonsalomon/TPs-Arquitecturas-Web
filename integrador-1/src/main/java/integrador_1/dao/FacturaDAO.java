package integrador_1.dao;

import integrador_1.entity.Factura;

import java.util.List;
import java.util.Optional;

public interface FacturaDAO {
    Factura findById(Long id);
    List<Factura> findAll();
    //y si el cliente no tiene facturas? y si no existe el cliente?
    Optional<List<Factura>> findByCliente(Long idCliente);

    void create(Factura f);
    void update(Factura f);
    void updateCliente(Long idFactura, Long nuevoIdCliente);
    void delete(Long id);

    void deleteAll();
}
