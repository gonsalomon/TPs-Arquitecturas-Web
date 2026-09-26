package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.dto.InscripcionDTO;
import org.entity.Inscripcion;
import org.repository.InscripcionRepository;
import org.entity.Estudiante;
import org.entity.Carrera;
import java.time.LocalDate;

public class InscripcionRepositoryImpl implements InscripcionRepository {
    private EntityManagerFactory emf;
    private static InscripcionRepositoryImpl instance;
    private EstudianteRepositoryImpl estudiante;
    private CarreraRepositoryImpl carrera;

    private InscripcionRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
        this.estudiante = EstudianteRepositoryImpl.getInstance(emf);
        this.carrera = CarreraRepositoryImpl.getInstance(emf);
    }

    public static InscripcionRepositoryImpl getInstance(EntityManagerFactory emf) {
        if(instance == null) {
            instance = new InscripcionRepositoryImpl(emf);
        }
        return instance;
    }


    /* --------------------------- CRUD --------------------------- */
    @Override
    public Inscripcion create(Integer idCarrera, Integer idEstudiante, Integer fechaInscripcion, Integer fechaGraduacion, Integer antiguedad) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Estudiante e = em.find(Estudiante.class, idEstudiante);
            Carrera c = em.find(Carrera.class, idCarrera);
            if (e == null) {
                throw new RuntimeException(
                        "No existe el estudiante con id " + idEstudiante
                );
            }

            if (c == null) {
                throw new RuntimeException(
                        "No existe la carrera con id " + idCarrera
                );
            }
            if (fechaInscripcion == null) {
                fechaInscripcion = LocalDate.now().getYear();
            }

            Inscripcion inscripcion = new Inscripcion(c,e,fechaInscripcion,fechaGraduacion,antiguedad);

            em.persist(inscripcion);
            em.getTransaction().commit();
            return inscripcion;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }


    @Override
    public void save(Inscripcion nueva) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(nueva);

            em.getTransaction().commit();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;

        } finally {
            em.close();
        }
    }
}
