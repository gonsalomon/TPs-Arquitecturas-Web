package integrador_1.utils;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.entity.Cliente;
import integrador_1.entity.Factura;
import integrador_1.entity.FacturaProducto;
import integrador_1.entity.Producto;
import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;

/**
 * TP1 - Ejercicio Integrador - Punto 2
 * Carga clientes.csv, productos.csv, facturas.csv y facturas-productos.csv
 * usando Apache Commons CSV y los DAO del proyecto.
 *
 * Las columnas se leen por nombre de header, no por posicion: si manana el CSV
 * viene con las columnas en otro orden, sigue funcionando.
 *
 * Requisito: correr parado en la carpeta integrador-1 (las rutas son relativas).
 */
public class CargarDatos {
    private static final String RES = "src/main/resources/";

    private final DAOFactory factory;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;

    public CargarDatos() {
        this.factory = DAOFactory.getInstance(DBType.MYSQL);

        this.clienteDAO = factory.createClienteDAO();
        this.productoDAO = factory.createProductoDAO();
        this.facturaDAO = factory.createFacturaDAO();
        this.facturaProductoDAO = factory.createFacturaProductoDAO();
    }

    /* Todo en una sola transaccion: si algo falla a mitad de camino no queda
     * media base cargada, y ademas evita un commit por cada uno de los ~3200
     * inserts.
     * El orden importa: primero las tablas sin FK y despues las que dependen.
     */
    public void run() {
        factory.runInTransaction(() -> {
            cargarClientes(RES + "clientes.csv");
            cargarProductos(RES + "productos.csv");
            cargarFacturas(RES + "facturas.csv");
            cargarFacturaProductos(RES + "facturas-productos.csv");
        });
    }

    private void cargarClientes(String ubicacion) {
        int n = 0;
        try (CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion))) {
            for (CSVRecord registro : registros) {
                Cliente cliente = new Cliente(
                        Integer.parseInt(registro.get("idCliente")),
                        registro.get("nombre"),
                        registro.get("email"));
                clienteDAO.create(cliente);
                n++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + ubicacion, e);
        }
        System.out.println("  clientes cargados: " + n);
    }

    private void cargarProductos(String ubicacion) {
        int n = 0;
        try (CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion))) {
            for (CSVRecord registro : registros) {
                Producto producto = new Producto(
                        Integer.parseInt(registro.get("idProducto")),
                        registro.get("nombre"),
                        Double.parseDouble(registro.get("valor")));
                productoDAO.create(producto);
                n++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + ubicacion, e);
        }
        System.out.println("  productos cargados: " + n);
    }

    private void cargarFacturas(String ubicacion) {
        int n = 0;
        try (CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion))) {
            for (CSVRecord registro : registros) {
                Factura factura = new Factura(
                        Integer.parseInt(registro.get("idFactura")),
                        Integer.parseInt(registro.get("idCliente")));
                facturaDAO.create(factura);
                n++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + ubicacion, e);
        }
        System.out.println("  facturas cargadas: " + n);
    }

    private void cargarFacturaProductos(String ubicacion) {
        int n = 0;
        try (CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion))) {
            for (CSVRecord registro : registros) {
                FacturaProducto facturaProducto = new FacturaProducto(
                        Integer.parseInt(registro.get("idFactura")),
                        Integer.parseInt(registro.get("idProducto")),
                        Integer.parseInt(registro.get("cantidad")));
                facturaProductoDAO.create(facturaProducto);
                n++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + ubicacion, e);
        }
        System.out.println("  facturas-productos cargados: " + n);
    }

    // permite resolver el punto 2 por separado, sin pasar por Main
    public static void main(String[] args) {
        DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
        try {
            new CrearEsquema().run();
            new BorrarDatos().run();
            new CargarDatos().run();
            System.out.println("Datos cargados exitosamente.");
        } finally {
            factory.shutdown();
        }
    }
}
