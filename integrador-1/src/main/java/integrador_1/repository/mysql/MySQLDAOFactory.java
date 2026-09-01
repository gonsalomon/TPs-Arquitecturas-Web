package integrador_1.repository.mysql;

import java.sql.Connection;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.factory.DAOFactory;

public class MySQLDAOFactory extends DAOFactory{
    @Override
    protected Connection getConnection() {
        return MySQLConnectionManager.getInstance().getConnection();
    }

    @Override
    protected void doShutdown() {
        MySQLConnectionManager.getInstance().shutdown();
    }

    @Override
    public ClienteDAO createClienteDAO(){
        return new MySQLClienteDAO(getConnection());
    }

    @Override
    public FacturaDAO createFacturaDAO() {
        // Devuelve la implementación concreta MySQL de ProductoDAO
        return new MySQLFacturaDAO(getConnection());
    }

    @Override
    public FacturaProductoDAO createFacturaProductoDAO() {
        // Devuelve la implementación concreta MySQL de ProductoDAO
        return new MySQLFacturaProductoDAO(getConnection());
    }

    @Override
    public ProductoDAO createProductoDAO() {
        // Devuelve la implementación concreta MySQL de ProductoDAO
        return new MySQLProductoDAO(getConnection());
    }
}
