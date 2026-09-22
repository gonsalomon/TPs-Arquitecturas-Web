package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.entity.Estudiante;
import org.repository.EstudianteRepository;

public class EstudianteRepositoryImpl implements EstudianteRepository {
    private EntityManagerFactory emf;
    private static EstudianteRepositoryImpl instance;

    private EstudianteRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static EstudianteRepositoryImpl getInstance(EntityManagerFactory emf) {
        if(instance == null) {
            instance = new EstudianteRepositoryImpl(emf);
        }
        return instance;
    }


    //    a) dar de alta un estudiante
    @Override
    public Estudiante create(Estudiante estudiante) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(estudiante);
            em.getTransaction().commit();
            return estudiante;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Estudiante findByDni(Integer dni) {
        EntityManager em = emf.createEntityManager();
        try{
            return em.find(Estudiante.class, dni);
        }  finally {
            em.close();
        }
    }
}
