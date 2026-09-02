package integrador_1.dao;

import integrador_1.dto.TopProductoRecaudador;
import integrador_1.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoDAO {
    Producto findById(int id);
    List<Producto> findAll();
    void create(Producto p);
    void update(Producto p);
    void delete(int id);
    void deleteAll();

    //no sé si tengo que traer algo de FacturaProducto así que lo dejo afuera del import... será el main que lo usa? Ni ideaaaa
    TopProductoRecaudador findProductMaxFacturacion();
}
