package integrador_1.utils;

import integrador_1.factory.DAOFactory;
import integrador_1.factory.DBType;

/**
 * TP1 - Ejercicio Integrador - Punto 1
 * Programa JDBC que crea el esquema de la base (cliente, producto, factura y
 * factura_producto, con sus PK y FK).
 *
 * Se puede correr solo, con su propio main, o invocarlo desde Main antes de la
 * carga de los CSV. El DDL concreto vive en la factory del motor
 * (MySQLDAOFactory.createSchema), asi que agregar otra base no obliga a tocar
 * esta clase.
 *
 * La base "Integrador" tampoco hace falta crearla a mano: la URL de conexion
 * usa createDatabaseIfNotExist=true.
 */
public class CrearEsquema {

    public void run() {
        DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
        factory.createSchema();
        System.out.println("Esquema creado: cliente, producto, factura, factura_producto.");
    }

    public static void main(String[] args) {
        DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
        try {
            new CrearEsquema().run();
        } finally {
            factory.shutdown();
        }
    }
}
