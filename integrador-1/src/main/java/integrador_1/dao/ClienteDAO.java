package integrador_1.dao;

import integrador_1.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO {
    //create
    void create(Cliente c);
    //read: n, 1
    Cliente findById(Long id);
    List<Cliente> findAll();
    //update
    void update(Cliente c);
    //delete: n, 1
    void delete(Long idCliente);
    void deleteAll();

    //especial p resolver ej 4: lista ordenada x facturacion
    Optional<List<Cliente>> sortClientesByFacturacion();
}
