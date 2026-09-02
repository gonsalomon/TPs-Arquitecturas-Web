package integrador_1.utils;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;
import integrador_1.repository.mysql.MySQLDAOFactory;

public class BorrarDatos {
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;

    public BorrarDatos(){
        DAOFactory factoryDAO = DAOFactory.getInstance(DBType.MYSQL);
        this.productoDAO=factoryDAO.createProductoDAO();
        this.clienteDAO=factoryDAO.createClienteDAO();
        this.facturaDAO=factoryDAO.createFacturaDAO();
        this.facturaProductoDAO=factoryDAO.createFacturaProductoDAO();
    }

    public void run(){
        try {
            this.facturaProductoDAO.deleteAll();
            this.productoDAO.deleteAll();
            this.facturaDAO.deleteAll();
            this.clienteDAO.deleteAll();

            System.out.println("Borrado completo de clientes, productos, facturas y productos-facturas.");
        }
        catch (Exception e) {
            throw new RuntimeException("Error durante el borrado masivo.", e);
        }
    }
}