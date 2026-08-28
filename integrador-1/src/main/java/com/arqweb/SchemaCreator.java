package com.arqweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TP1 - Ejercicio Integrador - Punto 1
 * me ayudé con Claude... si lo tengo que armar a manopla me complica un poco, tendré que memorizar lo de
 * connection, drivermanager, sqlexception, statement... no hay chance de usar esto en producción, sé bien
 * que se usa Spring Boot+Hibernate y te resuelve la vida...
 * 
 * requisitos: tener MySQL instalado (para instalar Docker tenía que formatear)
 */
public class SchemaCreator {

    // el flag de allowPublicKeyRetrieval lo googleé
    private static final String URL =
        "jdbc:mysql://localhost:3306/facturas_db" +
        "?createDatabaseIfNotExist=true" +
        "&useSSL=false" +
        "&serverTimezone=UTC" +
        "&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        // try-with-resources: cierra la conexión automáticamente al terminar
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("✓ Conectado a MySQL — base de datos: facturas_db");
            dropTablesIfExist(conn);
            createTables(conn);
            System.out.println("✓ Esquema creado exitosamente.");
        } catch (SQLException e) {
            System.err.println("✗ Error de base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //con esto bajo las tablas si ya estaban, para evitar que me arroje SQLException si corro dos veces el mismo programa
    private static void dropTablesIfExist(Connection conn) throws SQLException {
        String[] drops = {
            "DROP TABLE IF EXISTS Factura_Producto",
            "DROP TABLE IF EXISTS Factura",
            "DROP TABLE IF EXISTS Producto",
            "DROP TABLE IF EXISTS Cliente"
        };
        try (Statement stmt = conn.createStatement()) {
            for (String sql : drops) {
                stmt.execute(sql);
                System.out.println("  dropped: " + sql);
            }
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createCliente = """
                CREATE TABLE Cliente (
                    idCliente INT           NOT NULL,
                    nombre    VARCHAR(500)  NOT NULL,
                    email     VARCHAR(150),
                    PRIMARY KEY (idCliente)
                )
                """;
        String createProducto = """
                CREATE TABLE Producto (
                    idProducto INT          NOT NULL,
                    nombre     VARCHAR(45)  NOT NULL,
                    valor      FLOAT,
                    PRIMARY KEY (idProducto)
                )
                """;
        String createFactura = """
                CREATE TABLE Factura (
                    idFactura INT NOT NULL,
                    idCliente INT NOT NULL,
                    PRIMARY KEY (idFactura),
                    CONSTRAINT fk_factura_cliente
                        FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente)
                )
                """;
        String createFacturaProducto = """
                CREATE TABLE Factura_Producto (
                    idFactura  INT NOT NULL,
                    idProducto INT NOT NULL,
                    cantidad   INT,
                    PRIMARY KEY (idFactura, idProducto),
                    CONSTRAINT fk_fp_factura
                        FOREIGN KEY (idFactura)  REFERENCES Factura(idFactura),
                    CONSTRAINT fk_fp_producto
                        FOREIGN KEY (idProducto) REFERENCES Producto(idProducto)
                )
                """;
        //ordeno las tablas por prioridad de FK (necesito las PK independientes 1ro, luego puedo declarar las FK correspondientes)
        String[] creates = {
            createCliente,
            createProducto,
            createFactura,
            createFacturaProducto
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : creates) {
                stmt.execute(sql);
                // Imprime solo la primera línea (nombre de la tabla) para no saturar la consola
                String tableName = sql.strip().lines().findFirst().orElse("").trim();
                System.out.println("  created: " + tableName);
            }
        }
    }
}