package integrador_1.repository.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.factory.DAOFactory;

public class MySQLDAOFactory extends DAOFactory{

    /* Punto 1 del integrador: crea el esquema completo en un solo lugar.
     * El orden importa: primero las tablas sin FK y despues las que las referencian.
     */
    @Override
    public void createSchema() {
        final String[] ddl = {
                "CREATE TABLE IF NOT EXISTS cliente (" +
                        "idCliente INT PRIMARY KEY," +
                        "nombre VARCHAR(100) NOT NULL," +
                        "email VARCHAR(120) NOT NULL UNIQUE)",

                "CREATE TABLE IF NOT EXISTS producto (" +
                        "idProducto INT PRIMARY KEY," +
                        "nombre VARCHAR(45) NOT NULL," +
                        "valor DOUBLE NOT NULL)",

                "CREATE TABLE IF NOT EXISTS factura (" +
                        "idFactura INT PRIMARY KEY," +
                        "idCliente INT NOT NULL," +
                        "FOREIGN KEY (idCliente) REFERENCES cliente(idCliente) " +
                        "ON DELETE CASCADE ON UPDATE CASCADE)",

                "CREATE TABLE IF NOT EXISTS factura_producto (" +
                        "idFactura INT," +
                        "idProducto INT," +
                        "cantidad INT NOT NULL," +
                        "PRIMARY KEY (idFactura, idProducto)," +
                        "FOREIGN KEY (idFactura) REFERENCES factura(idFactura) " +
                        "ON DELETE CASCADE ON UPDATE CASCADE," +
                        "FOREIGN KEY (idProducto) REFERENCES producto(idProducto) " +
                        "ON DELETE CASCADE ON UPDATE CASCADE)"
        };
        try (Statement st = getConnection().createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creando el esquema de la base", e);
        }
    }
    @Override
    protected Connection getConnection() {
        return MySQLConnectionManager.getInstance().getConnection();
    }

    @Override
    protected void doShutdown() {
        MySQLConnectionManager.getInstance().shutdown();
    }

    /* Una sola transaccion para todo el bloque: commit al final, rollback si
     * algo explota. Evita los ~3200 commits implicitos de la carga de CSV.
     */
    @Override
    public void runInTransaction(Runnable work) {
        Connection cn = getConnection();
        try {
            cn.setAutoCommit(false);
            try {
                work.run();
                cn.commit();
            } catch (RuntimeException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error manejando la transaccion", e);
        }
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
