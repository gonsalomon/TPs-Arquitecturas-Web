package integrador_1.dao;

import integrador_1.dto.ClienteFacturacion;
import integrador_1.entity.Cliente;

import java.util.List;

public interface ClienteDAO {
    void create(Cliente c);
    Cliente findById(int id);
    List<Cliente> findAll();
    void update(Cliente c);
    void delete(int idCliente);
    void deleteAll();

    //especial p resolver ej 4: lista ordenada x facturacion
    List<ClienteFacturacion> sortClientesByFacturacion();
}
