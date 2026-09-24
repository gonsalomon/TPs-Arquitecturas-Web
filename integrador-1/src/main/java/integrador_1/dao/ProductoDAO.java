package integrador_1.dao;

import integrador_1.dto.TopProductoRecaudador;
import integrador_1.entity.Producto;

import java.util.List;

public interface ProductoDAO {
    Producto findById(int id);
    List<Producto> findAll();
    void create(Producto p);
    void update(Producto p);
    void delete(int id);
    void deleteAll();

    /* Ej 3. Devuelve un DTO y no un Producto porque la recaudacion se calcula
     * cruzando con factura_producto: la consulta vive en el DAO, no en el Main.
     */
    TopProductoRecaudador findProductMaxFacturacion();
}
