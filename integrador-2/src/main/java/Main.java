import jakarta.persistence.EntityManagerFactory;
import org.dto.CarreraDTO;
import org.dto.EstudianteDTO;
import org.entity.Carrera;
import org.entity.Estudiante;
import org.entity.Inscripcion;
import org.dto.ReporteDTO;
import org.factory.JPAUtil;
import org.repository.CarreraRepository;
import org.repository.EstudianteRepository;
import org.repository.InscripcionRepository;

import java.util.List;
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

    // 2)
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
    //C) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple
    System.out.println("\n=== Estudiantes ordenados por apellido ===");
    List<EstudianteDTO> estudiantesOrdenados = estudianteRepository.findAllOrderByApellido();
    for (EstudianteDTO e : estudiantesOrdenados){
        System.out.println(e);
    }

    //D) recuperar un estudiante, en base a su número de libreta universitaria.
    Integer lu = 256879;
    System.out.println("\n=== Estudiante recuperado por LU: " + lu + " ===");
    Estudiante estudiantePorLU = estudianteRepository.findByLU(lu);
    if (estudiantePorLU != null) {
        System.out.println("Dni: " + estudiantePorLU.getDNI() + " " + estudiantePorLU.getNombre()
                + " " + estudiantePorLU.getApellido() + ", LU: " + estudiantePorLU.getLU());
    } else {
        System.out.println("No existe ningun estudiante con LU " + lu);
    }

    //E) recuperar todos los estudiantes, en base a su género. (Male / Masculino / Female / Femenino).
        String genero = "Male";
        System.out.println("\n=== Estudiantes recuperados del genero: "+ genero +" ===");
        List<EstudianteDTO> estudiantesPorGeneroDTO = estudianteRepository.findByGender(genero);
        for (EstudianteDTO e : estudiantesPorGeneroDTO){
            System.out.println(e);
        }

    //F) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos.
    System.out.println("\n=== Carreras con inscriptos, ordenadas por cantidad ===");
    List<CarreraDTO> carrerasPorInscriptos = carreraRepository.findConInscriptosOrdenadasPorCantidad();
    for (CarreraDTO c : carrerasPorInscriptos) {
        System.out.println(c);
    }

    //G) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia.
    Integer idCarrera = 6;
    String ciudadResidencia = "Rauch";
    System.out.println("\n=== Estudiantes de la carrera " + idCarrera + " en " + ciudadResidencia + " ===");
    List<EstudianteDTO> estudiantesPorCarreraYCiudad = estudianteRepository.buscarPorCarreraYCiudad(idCarrera, ciudadResidencia);
    for (EstudianteDTO e : estudiantesPorCarreraYCiudad) {
        System.out.println(e);
    }

    //3) Generar un reporte de las carreras, que para cada carrera incluya información de los
    //inscriptos y egresados por año. Se deben ordenar las carreras alfabéticamente, y presentar
    //los años de manera cronológica.
    System.out.println("\n=== Reporte de carreras (inscriptos y egresados por anio) ===");
    List<ReporteDTO> reporteCarreras = carreraRepository.generarReporteCarreras();
    for (ReporteDTO r : reporteCarreras) {
        System.out.println(r);
    }

    }
}
