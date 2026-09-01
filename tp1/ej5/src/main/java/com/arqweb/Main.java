package com.arqweb;

import com.arqweb.entidades.Persona;

import javax.persistence.*;
import java.util.List;

/**
 * Ejercicio 5: JPA + MySQL.
 * Re-implementación de las consultas del ej2 usando EntityManager en lugar de JDBC.
 */
public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mysql_pu");
        EntityManager em = emf.createEntityManager();

        // INSERT
        em.getTransaction().begin();
        em.persist(new Persona("Juan",  "Perez",  30));
        em.persist(new Persona("Maria", "Garcia", 25));
        em.getTransaction().commit();
        System.out.println("Personas insertadas.");

        // SELECT todos
        List<Persona> personas = em.createQuery("SELECT p FROM Persona p", Persona.class)
                                   .getResultList();
        System.out.println("Lista completa:");
        personas.forEach(p -> System.out.println("  " + p));

        // SELECT por id
        Persona p = em.find(Persona.class, 1);
        System.out.println("Buscar id=1: " + p);

        em.close();
        emf.close();
    }
}