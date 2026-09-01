package integrador_1.dao;

import integrador_1.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO {
    Cliente findById(Long id);
    List<Cliente> findAll();
    List<Cliente> findByCliente(Long idCliente);

    void create(Cliente c);
    void update(Cliente c);
    void delete(Long idCliente);

    void deleteAll();

    Optional<List<Cliente>> sortClientesByFacturacion();
}
