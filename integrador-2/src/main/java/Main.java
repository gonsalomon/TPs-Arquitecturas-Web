import jakarta.persistence.EntityManagerFactory;
import org.dto.CarreraDTO;
import org.dto.EstudianteDTO;
import org.entity.Estudiante;
import org.dto.ReporteDTO;
import org.factory.JPAUtil;
import org.repository.CarreraRepository;
import org.repository.EstudianteRepository;
import org.repository.InscripcionRepository;
import java.util.List;
import java.util.Scanner;
import org.repository.impl.CarreraRepositoryImpl;
import org.repository.impl.EstudianteRepositoryImpl;
import org.repository.impl.InscripcionRepositoryImpl;
import org.utils.CargarDatos;

public class Main {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
        CargarDatos cargarDatos = new CargarDatos();

        // SINGLETON
        EstudianteRepository estudianteRepository = EstudianteRepositoryImpl.getInstance(emf);
        CarreraRepository carreraRepository = CarreraRepositoryImpl.getInstance(emf);
        InscripcionRepository inscripcionRepository = InscripcionRepositoryImpl.getInstance(emf);

        /* --------------------------- CARGA DE ARCHIVOS CSV -------------------------- */
        cargarDatos.addCarrera(carreraRepository);
        cargarDatos.addEstudiante(estudianteRepository);
        cargarDatos.addInscripcion(inscripcionRepository, estudianteRepository, carreraRepository);

        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n========== MENÚ ==========");
            System.out.println("1. Dar de alta un estudiante");
            System.out.println("2. Matricular un estudiante en una carrera");
            System.out.println("3. Recuperar estudiantes ordenados por apellido");
            System.out.println("4. Recuperar estudiante por LU");
            System.out.println("5. Recuperar estudiantes por género");
            System.out.println("6. Recuperar carreras con inscriptos ordenadas por cantidad");
            System.out.println("7. Recuperar estudiantes por carrera y ciudad");
            System.out.println("8. Generar reporte de carreras");
            System.out.println("0. Salir");
            System.out.print("Ingrese una opción: ");

            opcion = scanner.nextInt();

            switch (opcion) {

                case 1:
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

                    System.out.println("Estudiante dado de alta correctamente.");
                    break;

                case 2:
                    // B) Matricular un estudiante en una carrera
                    inscripcionRepository.create(6, 1, 2025, 0, 1);

                    System.out.println("Estudiante matriculado correctamente.");
                    break;

                case 3:
                    // C) Recuperar todos los estudiantes ordenados por apellido
                    System.out.println("\n=== Estudiantes ordenados por apellido ascendente ===");

                    List<EstudianteDTO> estudiantesOrdenados =
                            estudianteRepository.findAllOrderByApellidoASC();

                    for (EstudianteDTO e : estudiantesOrdenados) {
                        System.out.println(e);
                    }
                    break;

                case 4:
                    // D) Recuperar estudiante por LU
                    Integer lu = 43523;

                    System.out.println("\n=== Estudiante recuperado por LU: " + lu + " ===");

                    EstudianteDTO estudiantePorLU =
                            estudianteRepository.findByLU(lu);

                    if (estudiantePorLU != null) {
                        System.out.println(
                                "DNI: " + estudiantePorLU.getDNI()
                                        + " " + estudiantePorLU.getNombre()
                                        + " " + estudiantePorLU.getApellido()
                                        + ", LU: " + estudiantePorLU.getLU()
                        );
                    } else {
                        System.out.println(
                                "No existe ningún estudiante con LU " + lu
                        );
                    }
                    break;

                case 5:
                    // E) Recuperar estudiantes por género
                    String genero = "Male";

                    System.out.println(
                            "\n=== Estudiantes recuperados del género: "
                                    + genero + " ==="
                    );

                    List<EstudianteDTO> estudiantesPorGeneroDTO =
                            estudianteRepository.findByGender(genero);

                    for (EstudianteDTO e : estudiantesPorGeneroDTO) {
                        System.out.println(e);
                    }
                    break;

                case 6:
                    // F) Carreras con estudiantes inscriptos
                    System.out.println(
                            "\n=== Carreras con inscriptos, ordenadas por cantidad ==="
                    );

                    List<CarreraDTO> carrerasPorInscriptos =
                            carreraRepository.findConInscriptosOrdenadasPorCantidad();

                    for (CarreraDTO c : carrerasPorInscriptos) {
                        System.out.println(c);
                    }
                    break;

                case 7:
                    // G) Estudiantes de una carrera filtrados por ciudad
                    Integer idCarrera = 1;
                    String ciudadResidencia = "Rauch";

                    String carrera =
                            carreraRepository.findById(idCarrera).getNombre();

                    System.out.println(
                            "\n=== Estudiantes de la carrera "
                                    + carrera + " en " + ciudadResidencia + " ==="
                    );

                    List<EstudianteDTO> estudiantesPorCarreraYCiudad =
                            estudianteRepository.buscarPorCarreraYCiudad(
                                    idCarrera,
                                    ciudadResidencia
                            );

                    for (EstudianteDTO e : estudiantesPorCarreraYCiudad) {
                        System.out.println(e);
                    }
                    break;

                case 8:
                    // Reporte de carreras
                    System.out.println(
                            "\n=== Reporte de carreras (inscriptos y egresados por año) ==="
                    );

                    List<ReporteDTO> reporteCarreras =
                            carreraRepository.generarReporteCarreras();

                    for (ReporteDTO r : reporteCarreras) {
                        System.out.println(r);
                    }
                    break;

                case 0:
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
                    break;
            }
        } while (opcion != 0);
        scanner.close();
    }
}
