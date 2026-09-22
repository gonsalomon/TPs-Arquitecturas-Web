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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CargarDatos{
    public CargarDatos() {
    }

    /* ------------------------ MÉTODOS PARA CARGAR LOS CSV ----------------------- */
    public void addCarrera(CarreraRepository carrera) throws IOException {
        List<Carrera> listCarreras = new ArrayList<>();
        try (CSVParser carreras = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(Objects.requireNonNull(CargarDatos.class.getResourceAsStream("/carreras.csv"))))) {

            for (CSVRecord record : carreras) {
                String nombre = record.get("carrera");
                String duracion = record.get("duracion");

                listCarreras.add(new Carrera(nombre, Integer.valueOf(duracion)));
            }

            for (Carrera c : listCarreras) {
                carrera.create(c);
            }
        }
    }

    public void addEstudiante(EstudianteRepository estudiante) throws IOException {
        List<Estudiante> listEstudiantes = new ArrayList<>();
        try (CSVParser estudiantes = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(Objects.requireNonNull(CargarDatos.class.getResourceAsStream("/estudiantes.csv"))))) {

            for (CSVRecord record : estudiantes) {
                int dni = Integer.parseInt(record.get("DNI"));
                String nombre = record.get("nombre");
                String apellido = record.get("apellido");
                int edad = Integer.parseInt(record.get("edad"));
                String genero = record.get("genero");
                String ciudad = record.get("ciudad");
                int lu = Integer.parseInt(record.get("LU"));

                listEstudiantes.add(new Estudiante(dni, nombre, apellido, edad, genero, ciudad, lu));
            }

            for (Estudiante e : listEstudiantes) {
                estudiante.create(e);
            }
        }
    }

    public void addInscripcion(InscripcionRepository inscripcionRepo,EstudianteRepository estudianteRepo,CarreraRepository carreraRepo) throws IOException {
        try (CSVParser inscripciones = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(
                        Objects.requireNonNull(
                                CargarDatos.class.getResourceAsStream("/estudianteCarrera.csv")
                        )
                ))) {

            for (CSVRecord record : inscripciones) {

                int id = Integer.parseInt(record.get("id"));
                int idEstudiante = Integer.parseInt(record.get("id_estudiante"));
                int idCarrera = Integer.parseInt(record.get("id_carrera"));
                int inscripcion = Integer.parseInt(record.get("inscripcion"));
                int graduacion = Integer.parseInt(record.get("graduacion"));
                int antiguedad = Integer.parseInt(record.get("antiguedad"));

                // Buscar las entidades por ID
                Estudiante estudiante = estudianteRepo.findByDni(idEstudiante);
                Carrera carrera = carreraRepo.findById(idCarrera);

                if (estudiante == null) {
                    System.out.println("No existe el estudiante con id " + idEstudiante);
                    continue;
                }

                if (carrera == null) {
                    System.out.println("No existe la carrera con id " + idCarrera);
                    continue;
                }

                // Crear la inscripción usando las entidades
                Inscripcion nueva = new Inscripcion(
                        carrera,
                        estudiante,
                        inscripcion,
                        graduacion,
                        antiguedad
                );

                inscripcionRepo.save(nueva);
            }
        }
    }


}