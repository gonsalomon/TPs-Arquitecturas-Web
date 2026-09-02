package integrador_1;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.dto.TopProductoRecaudador;
import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;
import integrador_1.utils.BorrarDatos;
import integrador_1.utils.CargarDatos;

public class Main {
    private static final DBType MOTOR = DBType.MYSQL;
    public static void main(String[] args) {
        System.setProperty("db.type", System.getProperty("db.type", MOTOR.name()));
        System.out.println("=== Motor de base de datos: " + System.getProperty("db.type") + " ===");

        new BorrarDatos().run();
        System.out.println("Datos eliminados");

        new CargarDatos().run();
        System.out.println("Carga CSV finalizada");

        DAOFactory daoFactory = DAOFactory.getInstance();
        ProductoDAO productoDao = daoFactory.createProductoDAO();

        /*Escriba un programa JDBC que retorne el producto que más recaudó. Se define
        “recaudación” como cantidad de productos vendidos multiplicado por su valor*/

        System.out.println("Producto que más recaudó: ");
        TopProductoRecaudador productoRecaudador = productoDao.findProductMaxFacturacion();
        System.out.println(productoRecaudador);



        /*Escriba un programa JDBC que imprima una lista de clientes, ordenada por a cuál se le
        facturó más.*/

        ClienteDAO clienteDao = daoFactory.createClienteDAO();
        System.out.println("Lista de clientes ordenados por mayor facturacion");

        clienteDao.sortClientesByFacturacion();
        System.out.println(clienteDao);

    }
}
