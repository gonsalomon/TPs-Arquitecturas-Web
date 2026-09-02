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

public class CargarDatos {
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;
    private final FacturaDAO facturaDAO;
    private final FacturaProductoDAO facturaProductoDAO;

    public CargarDatos() {
        DAOFactory factoryDAO = DAOFactory.getInstance(DBType.MYSQL);

        this.clienteDAO = factoryDAO.createClienteDAO();
        this.productoDAO = factoryDAO.createProductoDAO();
        this.facturaDAO = factoryDAO.createFacturaDAO();
        this.facturaProductoDAO = factoryDAO.createFacturaProductoDAO();
    }


    public void run(){
        cargarClientes("src/main/resources/clientes.csv");
        cargarProductos("src/main/resources/productos.csv");
        cargarFacturas("src/main/resources/facturas.csv");
        cargarFacturaProductos("src/main/resources/facturas-productos.csv");
    }

    private void cargarProductos(String csv){
        try{
            CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(csv));
            for(CSVRecord registro:registros){
                Producto producto = new Producto(Integer.parseInt(registro.get(0)),
                        registro.get(1),
                        Double.parseDouble(registro.get(2)));
                productoDAO.create(producto);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void cargarClientes(String ubicacion){
        try{
            CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion));
            for(CSVRecord registro:registros){
                Cliente cliente=new Cliente(Integer.parseInt(registro.get(0)),
                        registro.get(1),
                        registro.get(2));
                clienteDAO.create(cliente);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void cargarFacturas(String ubicacion){
        try{
            CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion));
            for(CSVRecord registro:registros){
                Factura factura=new Factura(Integer.parseInt(registro.get(0)),
                        Integer.parseInt(registro.get(1)));

                facturaDAO.create(factura);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void cargarFacturaProductos(String ubicacion) {
        try {
            CSVParser registros = CSVFormat.DEFAULT.withHeader().parse(new FileReader(ubicacion));
            for (CSVRecord registro : registros) {
                FacturaProducto facturaProducto = new FacturaProducto(Integer.parseInt(registro.get(0)),
                        Integer.parseInt(registro.get(1)),
                        Integer.parseInt(registro.get(2)));

                facturaProductoDAO.create(facturaProducto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
