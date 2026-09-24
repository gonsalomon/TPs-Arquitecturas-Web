package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.dto.EstudianteDTO;
import org.entity.Estudiante;
import org.repository.EstudianteRepository;

import java.util.List;

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

    /* Obtener estudiantes por genero */
    @Override
    public List<EstudianteDTO> findByGender(String genero) {
        EntityManager em = emf.createEntityManager();
        List<EstudianteDTO> resultado = em.createQuery("SELECT new org.dto.EstudianteDTO(e.DNI, e.nombre, e.apellido, e.genero, e.edad, e.ciudad, e.LU)" +
                                        " FROM Estudiante e WHERE genero = :genero", EstudianteDTO.class)
                .setParameter("genero", genero)
                .getResultList();
        em.close();
        return resultado;
    }




}
