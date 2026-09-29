package org.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.entity.Carrera;
import org.entity.Estudiante;
import org.entity.Inscripcion;
import org.repository.CarreraRepository;
import org.repository.EstudianteRepository;
import org.repository.InscripcionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CargarDatos implements CommandLineRunner {

    private final CarreraRepository carreraRepo;
    private final EstudianteRepository estudianteRepo;
    private final InscripcionRepository inscripcionRepo;

    public CargarDatos(CarreraRepository carreraRepo,
                       EstudianteRepository estudianteRepo,
                       InscripcionRepository inscripcionRepo) {
        this.carreraRepo = carreraRepo;
        this.estudianteRepo = estudianteRepo;
        this.inscripcionRepo = inscripcionRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (carreraRepo.count() > 0) return;   // ya cargado, evita duplicados
        addCarrera();
        addEstudiante();
        addInscripcion();
    }

    private CSVParser abrir(String archivo) throws Exception {
        return CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(
                        Objects.requireNonNull(CargarDatos.class.getResourceAsStream(archivo))));
    }

    private void addCarrera() throws Exception {
        List<Carrera> lista = new ArrayList<>();
        try (CSVParser csv = abrir("/carreras.csv")) {
            for (CSVRecord r : csv) {
                lista.add(new Carrera(r.get("carrera"), Integer.valueOf(r.get("duracion"))));
            }
        }
        carreraRepo.saveAll(lista);
    }

    private void addEstudiante() throws Exception {
        List<Estudiante> lista = new ArrayList<>();
        try (CSVParser csv = abrir("/estudiantes.csv")) {
            for (CSVRecord r : csv) {
                lista.add(new Estudiante(
                        Integer.parseInt(r.get("DNI")), r.get("nombre"), r.get("apellido"),
                        Integer.parseInt(r.get("edad")), r.get("genero"), r.get("ciudad"),
                        Integer.parseInt(r.get("LU"))));
            }
        }
        estudianteRepo.saveAll(lista);
    }

    private void addInscripcion() throws Exception {
        List<Inscripcion> lista = new ArrayList<>();
        try (CSVParser csv = abrir("/estudianteCarrera.csv")) {
            for (CSVRecord r : csv) {
                int idEstudiante = Integer.parseInt(r.get("id_estudiante"));
                int idCarrera = Integer.parseInt(r.get("id_carrera"));

                Estudiante e = estudianteRepo.findById(idEstudiante).orElse(null);
                Carrera c = carreraRepo.findById(idCarrera).orElse(null);
                if (e == null) { System.out.println("No existe el estudiante con id " + idEstudiante); continue; }
                if (c == null) { System.out.println("No existe la carrera con id " + idCarrera); continue; }

                lista.add(new Inscripcion(c, e,
                        Integer.parseInt(r.get("inscripcion")),
                        Integer.parseInt(r.get("graduacion")),
                        Integer.parseInt(r.get("antiguedad"))));
            }
        }
        inscripcionRepo.saveAll(lista);
    }
}