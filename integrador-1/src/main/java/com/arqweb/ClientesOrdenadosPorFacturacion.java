package com.arqweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientesOrdenadosPorFacturacion {

    private static final String URL = "jdbc:mysql://localhost:3306/facturas_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        // Query: Calcula el total facturado por cada cliente y los ordena de mayor a menor.
        String sql = "SELECT c.idCliente, c.nombre, SUM(fp.cantidad * p.valor) AS total_facturado " +
                     "FROM Cliente c " +
                     "INNER JOIN Factura f ON c.idCliente = f.idCliente " +
                     "INNER JOIN Factura_Producto fp ON f.idFactura = fp.idFactura " +
                     "INNER JOIN Producto p ON fp.idProducto = p.idProducto " +
                     "GROUP BY c.idCliente, c.nombre " +
                     "ORDER BY total_facturado DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("=== Lista de clientes por total facturado (de mayor a menor) ===");
            int puesto = 1;
            boolean hayRegistros = false;

            while (rs.next()) {
                hayRegistros = true;
                int id = rs.getInt("idCliente");
                String nombre = rs.getString("nombre");
                double total = rs.getDouble("total_facturado");

                System.out.printf("%d° - ID: %d | Cliente: %s | Total Facturado: %.2f%n",
                        puesto++, id, nombre, total);
            }

            if (!hayRegistros) {
                System.out.println("No hay clientes con facturas cargadas.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos:");
            e.printStackTrace();
        }
    }
}