import jakarta.persistence.EntityManagerFactory;
import org.factory.JPAUtil;
import org.repository.CarreraRepository;
import org.repository.EstudianteRepository;
import org.repository.InscripcionRepository;
import org.repository.impl.CarreraRepositoryImpl;
import org.repository.impl.EstudianteRepositoryImpl;
import org.repository.impl.InscripcionRepositoryImpl;
import org.utils.CargarDatos;

public class Main {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
        CargarDatos cargarDatos = new CargarDatos();

        // SINGLETON (el repo, no el entityManager -> NO thread-safe)
        EstudianteRepository estudianteRepository = EstudianteRepositoryImpl.getInstance(emf);
        CarreraRepository carreraRepository = CarreraRepositoryImpl.getInstance(emf);
        InscripcionRepository inscripcionRepository = InscripcionRepositoryImpl.getInstance(emf);

        /* --------------------------- CARGA DE ARCHIVOS CSV -------------------------- */
        cargarDatos.addCarrera(carreraRepository);
        cargarDatos.addEstudiante(estudianteRepository);
        cargarDatos.addInscripcion(inscripcionRepository,estudianteRepository,carreraRepository);
    }

}
