package integrador_1.dao;

import integrador_1.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO {
    void create(Cliente c);
    Cliente findById(int id);
    List<Cliente> findAll();
    void update(Cliente c);
    void delete(int idCliente);
    void deleteAll();
    List<Cliente> sortClientesByFacturacion();
}
