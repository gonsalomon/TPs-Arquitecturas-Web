package integrador_1;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.dto.ClienteFacturacion;
import integrador_1.dto.TopProductoRecaudador;
import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;
import integrador_1.utils.BorrarDatos;
import integrador_1.utils.CargarDatos;
import integrador_1.utils.CrearEsquema;

import java.util.List;

/**
 * TP1 - Ejercicio Integrador.
 * Corre los cuatro puntos de punta a punta. Cada uno tambien se puede ejecutar
 * por separado: CrearEsquema y CargarDatos tienen su propio main.
 */
public class Main {
    private static final DBType MOTOR = DBType.MYSQL;

    public static void main(String[] args) {
        System.setProperty("db.type", System.getProperty("db.type", MOTOR.name()));
        System.out.println("=== Motor de base de datos: " + System.getProperty("db.type") + " ===");

        DAOFactory daoFactory = DAOFactory.getInstance();
        try {
            // Punto 1: crear el esquema de la base
            new CrearEsquema().run();

            // Punto 2: cargar los CSV (se limpia antes para poder correrlo N veces)
            new BorrarDatos().run();
            System.out.println("Datos eliminados");

            new CargarDatos().run();
            System.out.println("Carga CSV finalizada");

            /* Punto 3: producto que mas recaudo. "Recaudacion" = cantidad de
             * productos vendidos multiplicado por su valor.
             */
            ProductoDAO productoDao = daoFactory.createProductoDAO();
            System.out.println("\n--- Punto 3: producto que mas recaudo ---");
            TopProductoRecaudador productoRecaudador = productoDao.findProductMaxFacturacion();
            System.out.println(productoRecaudador);

            // Punto 4: clientes ordenados por a cual se le facturo mas
            ClienteDAO clienteDao = daoFactory.createClienteDAO();
            System.out.println("\n--- Punto 4: clientes ordenados por facturacion ---");
            List<ClienteFacturacion> clientesOrdenados = clienteDao.sortClientesByFacturacion();
            clientesOrdenados.forEach(System.out::println);
        } finally {
            // pase lo que pase, la conexion se cierra
            daoFactory.shutdown();
        }
    }
}
