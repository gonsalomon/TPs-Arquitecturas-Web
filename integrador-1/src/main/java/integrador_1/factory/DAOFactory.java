package integrador_1.factory;

import java.sql.Connection;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;
import integrador_1.repository.mysql.MySQLDAOFactory;

/* Abstract Factory + Singleton.
 * El integrador se resuelve solo con MySQL, pero toda la aplicacion habla
 * contra esta clase abstracta y contra las interfaces DAO: sumar otro motor es
 * agregar el valor a DBType, un case al switch de getInstance y la
 * implementacion concreta dentro de repository. Ninguna otra clase se entera.
 */
public abstract class DAOFactory {
    private static volatile DAOFactory instance;

    public static DAOFactory getInstance(DBType type) {
        if (instance == null) {
            synchronized (DAOFactory.class) {
                if (instance == null) {
                    switch (type) {
                        case MYSQL:
                            instance = new MySQLDAOFactory();
                            break;
//                        case DERBY:
//                            instance = new DerbyDAOFactory();
//                            break;
                        default:
                            throw new IllegalArgumentException("DBType no soportado: " + type);
                    }
                }
            }
        }
        return instance;
    }

    /* Variante sin parametros: lee la system property db.type (por ejemplo
     * -Ddb.type=MYSQL) y si no esta definida usa MYSQL por defecto.
     */
    public static DAOFactory getInstance() {
        String v = System.getProperty("db.type", "MYSQL");
        DBType type = DBType.valueOf(v.toUpperCase());
        return getInstance(type);
    }

    public abstract ClienteDAO createClienteDAO();
    public abstract FacturaDAO createFacturaDAO();
    public abstract FacturaProductoDAO createFacturaProductoDAO();
    public abstract ProductoDAO createProductoDAO();

    /* Punto 1: creacion del esquema. El DDL depende del motor, asi que se
     * declara aca y lo resuelve cada factory concreta.
     */
    public abstract void createSchema();

    /* Ejecuta un bloque de trabajo dentro de una unica transaccion (commit al
     * final, rollback si algo falla). Lo usa la carga de los CSV: con ~3200
     * inserts, hacer un commit por fila es lo que la vuelve lenta.
     */
    public abstract void runInTransaction(Runnable work);

    /* Protegido: la conexion es un detalle de cada motor y no sale de la
     * jerarquia de factories.
     */
    protected abstract Connection getConnection();

    // toda factory tiene que poder cerrar con certeza lo que abrio
    public final void shutdown() {
        doShutdown();
        synchronized (DAOFactory.class) {
            instance = null;
        }
    }

    // cada motor cierra a su manera, se delega
    protected abstract void doShutdown();
}
