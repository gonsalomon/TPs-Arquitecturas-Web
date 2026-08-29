package com.arqweb.tp1.ej2;

import java.sql.*;

/**
 * TP1 - Ejercicio 2
 * Mismas consultas del Ejercicio 1, pero contra MySQL en vez de Derby.
 * Requisito: tener un MySQL corriendo en localhost:3306 (ver docker-run.txt en esta carpeta).
 */
public class MySQLPersonaApp {

    private static final String URL =
            "jdbc:mysql://localhost:3306/persona_db" +
            "?createDatabaseIfNotExist=true" +
            "&useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conectado a MySQL - base persona_db.");
            crearTabla(conn);
            insertarDatosDePrueba(conn);
            ejecutarConsultas(conn);
        }
    }

    private static void crearTabla(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Persona (" +
                    "id INT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "edad INT)");
            System.out.println("Tabla Persona lista.");
        }
    }

    private static void insertarDatosDePrueba(Connection conn) throws SQLException {
        // INSERT IGNORE es el equivalente en MySQL al catch de PK duplicada que usamos en Derby (ej1)
        String sql = "INSERT IGNORE INTO Persona (id, nombre, edad) VALUES (?, ?, ?)";
        Object[][] datos = {
                {1, "Ana", 28},
                {2, "Bruno", 34},
                {3, "Carla", 22},
                {4, "Diego", 41}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] fila : datos) {
                ps.setInt(1, (int) fila[0]);
                ps.setString(2, (String) fila[1]);
                ps.setInt(3, (int) fila[2]);
                ps.executeUpdate();
            }
        }
    }

    private static void ejecutarConsultas(Connection conn) throws SQLException {
        // Las mismas 4 consultas del Ejercicio 1, para poder comparar Derby vs MySQL con el mismo criterio.
        System.out.println("=== Todas las personas ===");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Persona ORDER BY id")) {
            while (rs.next()) {
                System.out.printf("%d - %s (%d años)%n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad"));
            }
        }

        System.out.println("=== Mayores de 30 (ordenadas por edad desc) ===");
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nombre, edad FROM Persona WHERE edad > ? ORDER BY edad DESC")) {
            ps.setInt(1, 30);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%s (%d años)%n", rs.getString("nombre"), rs.getInt("edad"));
                }
            }
        }

        System.out.println("=== Total de personas cargadas ===");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Persona")) {
            if (rs.next()) System.out.println("Total: " + rs.getInt("total"));
        }

        System.out.println("=== Actualización: cumpleaños de Ana (id=1) ===");
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Persona SET edad = edad + 1 WHERE id = ?")) {
            ps.setInt(1, 1);
            int filas = ps.executeUpdate();
            System.out.println("Filas actualizadas: " + filas);
        }
    }
}
