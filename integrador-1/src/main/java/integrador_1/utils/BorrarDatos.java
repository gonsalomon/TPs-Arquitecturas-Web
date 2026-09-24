package integrador_1.utils;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;

/**
 * Vacia las cuatro tablas para poder correr la carga las veces que haga falta
 * sin chocar contra las PK.
 */
public class BorrarDatos {
    private final DAOFactory factory;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;

    public BorrarDatos() {
        this.factory = DAOFactory.getInstance(DBType.MYSQL);
        this.productoDAO = factory.createProductoDAO();
        this.clienteDAO = factory.createClienteDAO();
        this.facturaDAO = factory.createFacturaDAO();
        this.facturaProductoDAO = factory.createFacturaProductoDAO();
    }

    /* De las hojas hacia la raiz: primero la tabla intermedia, despues las que
     * son referenciadas por ella y por ultimo cliente.
     */
    public void run() {
        factory.runInTransaction(() -> {
            this.facturaProductoDAO.deleteAll();
            this.facturaDAO.deleteAll();
            this.productoDAO.deleteAll();
            this.clienteDAO.deleteAll();
        });
        System.out.println("Borrado completo de clientes, productos, facturas y productos-facturas.");
    }
}
