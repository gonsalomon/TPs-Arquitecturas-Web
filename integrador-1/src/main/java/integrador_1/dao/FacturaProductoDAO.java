package integrador_1.dao;

import integrador_1.entity.FacturaProducto;

import java.util.List;
import java.util.Optional;

public interface FacturaProductoDAO {
    FacturaProducto findById(int id);
    List<FacturaProducto> findAll();
    //y si un producto nunca se facturó?
    Optional<List<FacturaProducto>> findByProducto(int idProducto);

    void create(FacturaProducto fp);
    void update(FacturaProducto fp);
    //idFactura es PK, pero la FK se tiene que poder updatear
    void updateProducto(int id, int idProducto);
    void delete(int id);

    void deleteAll();
}
