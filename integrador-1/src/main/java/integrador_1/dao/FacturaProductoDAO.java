package integrador_1.dao;

import integrador_1.entity.FacturaProducto;

import java.util.List;
import java.util.Optional;

public interface FacturaProductoDAO {
    FacturaProducto findById(Long id);
    List<FacturaProducto> findAll();
    //y si un producto nunca se facturó?
    Optional<List<FacturaProducto>> findByProducto(Long idProducto);

    void create(FacturaProducto fp);
    void update(FacturaProducto fp);
    //idFactura es PK, pero la FK se tiene que poder updatear
    void updateProducto(Long id, Long idProducto);
    void delete(Long id);

    void deleteAll();
}
