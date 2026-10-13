# Integrador 3: Registro de Estudiantes - API REST

Proyecto de gestión de estudiantes y carreras universitarias desarrollado con Spring Boot. El sistema permite registrar estudiantes, inscribirlos en carreras, consultar inscripciones, filtrar estudiantes por carrera y ciudad, y generar reportes de inscriptos y egresados.

## Objetivos del proyecto

- Diseñar y desarrollar una API REST que gestione estudiantes, carreras e inscripciones.
- Implementar consultas personalizadas mediante JPQL y Spring Data JPA.
- Exponer los endpoints de manera clara y estructurada utilizando controladores REST.
- Aplicar buenas prácticas de arquitectura, separación de capas y uso de patrones de diseño.

## Tecnologías

- **Java 21**
- **Spring Boot**
  - **Spring Web** (para la API REST)
  - **Spring Data JPA e Hibernate** (para persistencia)
  - **Spring Boot Dev Tools** (recarga automática y mejoras en desarrollo)
- **JPA / Hibernate** (API para implementar mapeo objeto-relacional)
- **JPQL** (para hacer consultas de persistencia en Java (JPA)
- **MySQL**  (como base de datos)
- **Maven** (como gestor de dependencias)
- **Lombok** para reducir código repetitivo
- **Docker** (para levantar la base de datos)
- **Postman** (para testeo de endpoints)

## ️ Arquitectura del sistema
El sistema sigue una **arquitectura en capas**, compuesta por:

- **Controller** → gestiona las solicitudes HTTP y las respuestas.
- **Service** → contiene la lógica de negocio y las validaciones.
- **Repository** → maneja el acceso a los datos mediante *Spring Data JPA*.
- **DTOs (Data Transfer Objects)** → permiten transferir datos entre capas sin exponer directamente las entidades.
- **GlobalExceptionHandler** → manejo centralizado de errores y excepciones.

## Patrones de diseño aplicados
En el desarrollo del sistema se utilizaron los siguientes patrones de diseño arquitectónico:
- **Repository**: para aislar la persistencia de una entidad.
- **DTO (Data Transfer Object)**: transferencia de datos entre capas de la aplicación, desacoplando la entidad de la lógica de negocio (ocultando la información de mis entities). → seguridad, separación de intereses, performance.

## Endpoints

Todas las respuestas y solicitudes con cuerpo usan JSON. Los parámetros de ruta y query se muestran entre llaves.

### Estudiantes

| Método | Endpoint | Descripción | Respuesta exitosa                                   |
| --- | --- | --- |-----------------------------------------------------|
| `POST` | `/estudiantes` | Da de alta un estudiante. El DNI y la LU son obligatorios y no pueden estar duplicados. | `201 Created`, estudiante creado                    |
| `GET` | `/estudiantes` | Lista todos los estudiantes ordenados por apellido y luego por nombre. | `200 OK`, arreglo de estudiantes                    |
| `GET` | `/estudiantes/lu/{lu}` | Busca un estudiante por libreta universitaria. | `200 OK`, estudiante                                |
| `GET` | `/estudiantes/genero/{genero}` | Lista estudiantes del género indicado. | `200 OK`, arreglo de estudiantes                    |
| `GET` | `/estudiantes/filtro?ciudad={ciudad}&carrera={carrera}` | Filtra estudiantes por ciudad de residencia y carrera. | `200 OK`, arreglo de estudiantes; puede estar vacío |

Ejemplo para `POST /estudiantes`:

```json
{
  "dni": 100000001,
  "nombre": "Marta",
  "apellido": "Lopez",
  "edad": 23,
  "genero": "Femenino",
  "ciudad": "Tandil",
  "lu": 900001
}
```

El filtro se puede invocar, por ejemplo, como `GET /estudiantes/filtro?ciudad=Rauch&carrera=TUDAI`. Los valores de género dependen de los datos cargados; `male` es un valor utilizado por la colección Postman.

### Inscripciones

| Método | Endpoint | Descripción | Respuesta exitosa |
| --- | --- | --- | --- |
| `POST` | `/inscripciones` | Matricula un estudiante en una carrera. La fecha de inscripción se establece con el año actual. | `201 Created`, inscripción creada |

Ejemplo para `POST /inscripciones`:

```json
{
  "dni": 100000001,
  "idCarrera": 1
}
```

### Carreras

| Método | Endpoint | Descripción | Respuesta exitosa |
| --- | --- | --- | --- |
| `GET` | `/carreras/{id}` | Busca una carrera por identificador. | `200 OK`, carrera |
| `GET` | `/carreras/carreras-inscriptos` | Devuelve las carreras con inscriptos, ordenadas por cantidad descendente. | `200 OK`, arreglo con nombre y cantidad de inscriptos |
| `GET` | `/carreras/reporte` | Reporta inscriptos y egresados por carrera y año; carreras alfabéticamente y años cronológicamente. | `200 OK`, arreglo de filas del reporte |

Ejemplo de objeto de carrera:

```json
{
  "idCarrera": 2,
  "nombre": "Abogacia",
  "duracion": 4
}
```

El endpoint de reporte devuelve objetos con `carrera`, `anio`, `inscriptos` y `egresados`. El listado de carreras con inscriptos devuelve objetos con `nombre` y `cantidadInscriptos`.

## Códigos HTTP

| Código                      | Uso |
|-----------------------------| --- |
| `200 OK`                    | Consultas exitosas. |
| `201 Created`               | Recurso creado exitosamente. |
| `400 Bad Request`           | Solicitud inválida; por ejemplo, alta sin DNI o LU. |
| `404 Not Found`             | No existe el estudiante o la carrera solicitados. |
| `409 Conflict`              | DNI o LU duplicados, matrícula repetida o conflicto de integridad de datos. |
| `500 Internal Server Error` | Error del servidor. |

Los errores manejados por la aplicación devuelven un objeto JSON con `timestamp`, `error` y `message`.

