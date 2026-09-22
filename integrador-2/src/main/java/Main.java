import jakarta.persistence.EntityManagerFactory;
import org.entity.Carrera;
import org.entity.Estudiante;
import org.entity.Inscripcion;
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
        cargarDatos.addInscripcion(inscripcionRepository, estudianteRepository, carreraRepository);


    // A) Dar de alta un estudiante
    Estudiante e1 = new Estudiante();
        e1.setDNI(00000001);
        e1.setNombre("Roberto");
        e1.setApellido("Lopez");
        e1.setEdad(21);
        e1.setGenero("Masculino");
        e1.setCiudad("Tandil");
        e1.setLU(256879);
        estudianteRepository.create(e1);

    //B) matricular un estudiante en una carrer
        inscripcionRepository.create(6,23322529, 2025, 0, 1);
    //C) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple.

    //D) recuperar un estudiante, en base a su número de libreta universitaria.

    //E) recuperar todos los estudiantes, en base a su género.

    //F) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos.

    //G) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia.

    //Generar un reporte de las carreras, que para cada carrera incluya información de los
    //inscriptos y egresados por año. Se deben ordenar las carreras alfabéticamente, y presentar
    //los años de manera cronológica.

    }
}
