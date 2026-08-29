package com.arqweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TP1 - Ejercicio Integrador - Punto 3
 * Retorna el producto que más recaudó (suma de cantidad * valor).
 * Requisito: haber corrido SchemaCreator y DataLoader primero.
 */
public class TopProductoRecaudador {

    private static final String URL =
            "jdbc:mysql://localhost:3306/facturas_db" +
            "?useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        // Query: Calcula la recaudación total (cantidad * valor) por producto,
        // ordena de mayor a menor y devuelve solo el primero.
        String sql = "SELECT p.idProducto, p.nombre, SUM(fp.cantidad * p.valor) AS recaudacion " +
                     "FROM Factura_Producto fp " +
                     "INNER JOIN Producto p ON fp.idProducto = p.idProducto " +
                     "GROUP BY p.idProducto, p.nombre " +
                     "ORDER BY recaudacion DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt("idProducto");
                String nombre = rs.getString("nombre");
                double recaudacion = rs.getDouble("recaudacion");

                System.out.println("=== Producto con mayor recaudación ===");
                System.out.printf("ID: %d | Nombre: %s | Recaudación Total: %.2f%n", id, nombre, recaudacion);
            } else {
                System.out.println("No hay productos con ventas registradas.");
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos:");
            e.printStackTrace();
        }
    }
}
