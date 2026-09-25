package org.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.dto.CarreraDTO;
import org.dto.ReporteDTO;
import org.entity.Carrera;
import org.repository.CarreraRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    //F: recupero las carreras con inscriptos y ordeno por cantidad de inscriptos
    @Override
    public List<CarreraDTO> findConInscriptosOrdenadasPorCantidad() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT new org.dto.CarreraDTO(c.nombre, COUNT(i)) FROM Carrera c JOIN c.alumnosInscriptos i " +
                                    "GROUP BY c.nombre ORDER BY COUNT(i) DESC", CarreraDTO.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Punto 3: reporte de carreras con inscriptos y egresados por año.
    // Las consultas (conteo y agrupamiento) se resuelven en JPQL; en Java solo
    // se combinan los dos resultados (inscriptos por año y egresados por año)
    // en una sola estructura para poder imprimirlos juntos.
    @Override
    public List<ReporteDTO> generarReporteCarreras() {
        EntityManager em = emf.createEntityManager();
        try {
            // Todas las carreras (para que también aparezcan, sin inscriptos, las que no tengan ninguno)
            List<String> todasLasCarreras = em.createQuery(
                    "SELECT c.nombre FROM Carrera c ORDER BY c.nombre ASC", String.class)
                    .getResultList();

            // Inscriptos por carrera y año de inscripción
            List<Object[]> inscriptosPorAnio = em.createQuery(
                    "SELECT c.nombre, i.inscripcion, COUNT(i) " +
                            "FROM Inscripcion i JOIN i.carrera c " +
                            "WHERE i.inscripcion IS NOT NULL " +
                            "GROUP BY c.nombre, i.inscripcion " +
                            "ORDER BY c.nombre ASC, i.inscripcion ASC", Object[].class)
                    .getResultList();

            // Egresados por carrera y año de graduación (graduacion = 0 significa que no egresó)
            List<Object[]> egresadosPorAnio = em.createQuery(
                    "SELECT c.nombre, i.graduacion, COUNT(i) " +
                            "FROM Inscripcion i JOIN i.carrera c " +
                            "WHERE i.graduacion IS NOT NULL AND i.graduacion <> 0 " +
                            "GROUP BY c.nombre, i.graduacion " +
                            "ORDER BY c.nombre ASC, i.graduacion ASC", Object[].class)
                    .getResultList();

            // carrera (orden alfabetico) -> año (orden cronologico) -> {inscriptos, egresados}
            Map<String, TreeMap<Integer, long[]>> datos = new TreeMap<>();
            for (String nombreCarrera : todasLasCarreras) {
                datos.put(nombreCarrera, new TreeMap<>());
            }

            for (Object[] fila : inscriptosPorAnio) {
                String carrera = (String) fila[0];
                Integer anio = (Integer) fila[1];
                long cantidad = (Long) fila[2];
                datos.computeIfAbsent(carrera, k -> new TreeMap<>())
                        .computeIfAbsent(anio, k -> new long[2])[0] += cantidad;
            }

            for (Object[] fila : egresadosPorAnio) {
                String carrera = (String) fila[0];
                Integer anio = (Integer) fila[1];
                long cantidad = (Long) fila[2];
                datos.computeIfAbsent(carrera, k -> new TreeMap<>())
                        .computeIfAbsent(anio, k -> new long[2])[1] += cantidad;
            }

            List<ReporteDTO> reporte = new ArrayList<>();
            for (Map.Entry<String, TreeMap<Integer, long[]>> entradaCarrera : datos.entrySet()) {
                List<ReporteDTO.DetalleAnio> detalle = new ArrayList<>();
                for (Map.Entry<Integer, long[]> entradaAnio : entradaCarrera.getValue().entrySet()) {
                    long[] cantidades = entradaAnio.getValue();
                    detalle.add(new ReporteDTO.DetalleAnio(entradaAnio.getKey(), cantidades[0], cantidades[1]));
                }
                reporte.add(new ReporteDTO(entradaCarrera.getKey(), detalle));
            }
            return reporte;
        } finally {
            em.close();
        }
    }
}
