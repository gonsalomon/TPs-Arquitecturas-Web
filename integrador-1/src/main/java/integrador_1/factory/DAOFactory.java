package integrador_1.factory;

import java.sql.Connection;

import integrador_1.dao.ClienteDAO;
import integrador_1.dao.FacturaDAO;
import integrador_1.dao.FacturaProductoDAO;
import integrador_1.dao.ProductoDAO;

/* no usamos más que mysql para resolver este integrador, pero sabemos bien que podemos meter otras bases de datos en JDBC
* y también cómo hacer eso (sumamos el tipo a DBType, añadimos el tipo al switch en getInstance, resolvemos la implementación
* de la db concreta que hayamos sumado dentro de la carpeta repository)
* 
* pero ese es el motivo por el que sólo se usa la DAOFactory concreta de MySQL: no usamos ninguna otra para resolver el integrador
*/
import integrador_1.repository.mysql.MySQLDAOFactory;

public abstract class DAOFactory {
    private static volatile DAOFactory instance;
    
    /* el switch acá adentro permite que uses distintas bases de datos, enumeradas en DBType.java. 
    * Estamos usando solamente MySQL porque para qué más, pero la factory es capaz de lidiar con 
    * más de un tipo de base de datos (hence, the reason it's applying abstract factory; this is 
    * the power of it being abstract)
    * 
    * none of this is needed anymore though, since we got a lovely framework called Spring Boot
    * that solves all of this behind curtains nótese mi espanglish en manners of speaking
    * nadie dice "detrás del cortinado" en inglés
    */
    public static DAOFactory getInstance(DBType type){
        if(instance == null){
            synchronized (DAOFactory.class){
                if(instance == null){
                    switch (type) {
                        case MYSQL:
                            instance = new MySQLDAOFactory();
                            break;
                        default:
                            throw new IllegalArgumentException("DBType no soportado: " + type);
                    }
                }
            }
        }
        return instance;
    }

    //distinta signatura por si no pasan el tipo
    public static DAOFactory getInstance() {
        String v = System.getProperty("db.type", "MYSQL");  // lee una “system property” llamada db.type. Si no existe, usa "MYSQL" como valor por defecto.
        DBType type = DBType.valueOf(v.toUpperCase());
        return getInstance(type);
    }

    public abstract ClienteDAO createClienteDAO();
    public abstract FacturaDAO createFacturaDAO();
    public abstract FacturaProductoDAO createFacturaProductoDAO();
    public abstract ProductoDAO createProductoDAO();

    /* que sea protected hace que tanto esta clase como las hijas puedan llamar
    * y esto tiene sentido al pensar que todas las factory emplean este abstract
    * las factory puntuales de cada DB están en repository
    * 
    * igual persisto en afirmar: para qué más que una db concreta, con MySQL solo ya tenemos persistencia
    * aunque entendemos la necesidad de varias, la flexibilidad del abstract factory + factory method + singleton
    */
    protected abstract Connection getConnection();

    //todas las DAOfactory tienen que tener manera de cerrar con certeza todo lo que hayan arrancado
    public final void shutdown(){
        doShutdown();
        synchronized (DAOFactory.class){
            instance = null;
        }
    }

    //cada db tiene su propia manera de cerrar conexiones y se delega
    protected abstract void doShutdown();
}
