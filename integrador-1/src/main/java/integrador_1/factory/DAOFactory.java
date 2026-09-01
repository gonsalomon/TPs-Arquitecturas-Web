package integrador_1.factory;

import java.sql.Connection;

import integrador_1.dao.*;

import integrador_1.repository.mysql.MySQLDAOFactory;

public abstract class DAOFactory {
    private static volatile DAOFactory instance;
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
    }
}
