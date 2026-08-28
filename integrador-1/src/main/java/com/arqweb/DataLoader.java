package com.arqweb;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * TP1 - Ejercicio Integrador - Punto 1
 * me ayudé con Claude... si lo tengo que armar a manopla me complica un poco.
 * 
 * requisitos: haber corrido SchemaCreator primero
 */
public class DataLoader {

    private static final String URL =
            "jdbc:mysql://localhost:3306/facturas_db" +
            "?useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "admin";

    // Carpeta donde están los CSVs, relativa a la raíz del proyecto
    private static final String RES = "res/";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("✓ Conectado a MySQL.");

            // Orden importa: primero tablas sin FK, después las que dependen de ellas
            loadClientes(conn);
            loadProductos(conn);
            loadFacturas(conn);
            loadFacturasProductos(conn);

            System.out.println("✓ Datos cargados exitosamente.");
        } catch (SQLException | IOException e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadClientes(Connection conn) throws SQLException, IOException {
        String sql = "INSERT INTO Cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        // withFirstRecordAsHeader() es obsoleto, pero así tomo que los CSV traen la primera fila como nombre de columna
        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new FileReader(RES + "clientes.csv"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (CSVRecord row : parser) {
                ps.setInt(1,    Integer.parseInt(row.get("idCliente")));
                ps.setString(2, row.get("nombre"));
                ps.setString(3, row.get("email"));
                ps.addBatch(); // acumula el insert en lugar de enviarlo de a uno
            }
            ps.executeBatch(); // manda todos los inserts juntos — mucho más rápido
            System.out.println("  clientes cargados: " + parser.getRecordNumber() + " filas.");
        }
    }

    private static void loadProductos(Connection conn) throws SQLException, IOException {
        String sql = "INSERT INTO Producto (idProducto, nombre, valor) VALUES (?, ?, ?)";

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new FileReader(RES + "productos.csv"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (CSVRecord row : parser) {
                ps.setInt(1,    Integer.parseInt(row.get("idProducto")));
                ps.setString(2, row.get("nombre"));
                ps.setFloat(3,  Float.parseFloat(row.get("valor")));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  productos cargados: " + parser.getRecordNumber() + " filas.");
        }
    }

    private static void loadFacturas(Connection conn) throws SQLException, IOException {
        String sql = "INSERT INTO Factura (idFactura, idCliente) VALUES (?, ?)";

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new FileReader(RES + "facturas.csv"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (CSVRecord row : parser) {
                ps.setInt(1, Integer.parseInt(row.get("idFactura")));
                ps.setInt(2, Integer.parseInt(row.get("idCliente")));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  facturas cargadas: " + parser.getRecordNumber() + " filas.");
        }
    }

    private static void loadFacturasProductos(Connection conn) throws SQLException, IOException {
        String sql = "INSERT INTO Factura_Producto (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new FileReader(RES + "facturas-productos.csv"));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (CSVRecord row : parser) {
                ps.setInt(1, Integer.parseInt(row.get("idFactura")));
                ps.setInt(2, Integer.parseInt(row.get("idProducto")));
                ps.setInt(3, Integer.parseInt(row.get("cantidad")));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("  facturas-productos cargados: " + parser.getRecordNumber() + " filas.");
        }
    }
}