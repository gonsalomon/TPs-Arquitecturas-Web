package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
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

    //C: traemos todos los estudiantes, ordenados por apellido como criterio simple de ordenamiento
    @Override
    public List<EstudianteDTO> findAllOrderByApellido() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT new org.dto.EstudianteDTO(e.DNI, e.nombre, e.apellido, e.genero, e.edad, e.ciudad, e.LU)" +
                            " FROM Estudiante e ORDER BY e.apellido ASC", EstudianteDTO.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //D: buscamos un estudiante por su numero de libreta universitaria (LU)
    @Override
    public Estudiante findByLU(Integer lu) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Estudiante> resultado = em.createQuery(
                            "SELECT e FROM Estudiante e WHERE e.LU = :lu", Estudiante.class)
                    .setParameter("lu", lu)
                    .getResultList();
            // Si no hay ningun estudiante con ese LU, devolvemos null en vez de lanzar excepcion
            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    //G: recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia
    @Override
    public List<EstudianteDTO> buscarPorCarreraYCiudad(Integer carreraId, String ciudad) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT DISTINCT new org.dto.EstudianteDTO(" +
                    "e.DNI, e.nombre, e.apellido, e.genero, e.edad, e.ciudad, e.LU) " +
                    "FROM Inscripcion i " +
                    "JOIN i.estudiante e " +
                    "JOIN i.carrera c " +
                    "WHERE c.idCarrera = :carreraId AND e.ciudad = :ciudad";

            TypedQuery<EstudianteDTO> query = em.createQuery(jpql, EstudianteDTO.class);
            query.setParameter("carreraId", carreraId);
            query.setParameter("ciudad", ciudad);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
