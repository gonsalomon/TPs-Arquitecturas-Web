package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.entity.Carrera;
import org.repository.CarreraRepository;

public class CarreraRepositoryImpl implements CarreraRepository {
    private EntityManagerFactory emf;
    private static CarreraRepositoryImpl instance;

    private CarreraRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static CarreraRepositoryImpl getInstance(EntityManagerFactory emf) {
        if(instance == null) {
            instance = new CarreraRepositoryImpl(emf);
        }
        return instance;
    }

    @Override
    public Carrera create(Carrera carrera) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if(carrera.getIdCarrera() == null){
                em.persist(carrera);
            } else {
                em.merge(carrera);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
        return carrera;
    }

    @Override
    public Carrera findById(Integer idCarrera) {
        EntityManager em = emf.createEntityManager();
        try{
            return em.find(Carrera.class, idCarrera);
        }  finally {
            em.close();
        }
    }
}
