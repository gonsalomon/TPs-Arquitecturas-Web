package org.factory;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static final EntityManagerFactory emf;

    // Se inicializa una sola vez (Singleton)
    static {
        try {
            emf = Persistence.createEntityManagerFactory("integrador2");
        } catch (Throwable ex) {
            System.err.println("Error al crear EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Para obtener la única instancia del EntityManagerFactory
    //Esto permite hacer: ---> EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    // Para cerrar el factory cuando termine
    public static void close() {
        emf.close();
    }
}
