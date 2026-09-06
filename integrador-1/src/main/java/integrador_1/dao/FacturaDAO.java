package integrador_1.dao;

import integrador_1.entity.Factura;

import java.util.List;

public interface FacturaDAO {
    //CRUD
    Factura findById(int id);
    List<Factura> findAll();
    void create(Factura f);
    void update(Factura f);
    void delete(int id);
    void deleteAll();
}