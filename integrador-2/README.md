# Integrador 2: gestión de estudiantes y carreras

Proyecto de gestión de estudiantes y carreras universitarias.  
El sistema desarrollado permite registrar estudiantes, inscribirlos en carreras, consultar inscripciones y verificar si un estudiante se ha graduado.

## Contenidos
- [Objetivos_del_proyecto](#objetivos-del-proyecto)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Modelo de datos](#modelo-de-datos)
- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Cómo_levantar_el_proyecto](#cómo-levantar-el-proyecto)
- [Autores_del_proyecto](#autores-del-proyecto)

## Objetivos del proyecto

- Diseñar un modelo de objetos y un diagrama DER para el registro de estudiantes.
- Implementar consultas con **JPQL** para dar de alta, inscribir y recuperar información de estudiantes y carreras.
- Generar un reporte de carreras con inscriptos y egresados por año.
- Utilizar los patrones de diseño de arquitectura aprendidos

## Estructura del proyecto

```text
src/
├── diagramas/      # Diagramas de clases y DER
├── main/
│   ├── java/
│   │   ├── Main.java
│   │   └── org/
│   │       ├── dto/         # Objetos para transportar resultados
│   │       ├── entity/      # Entidades JPA
│   │       ├── factory/     # Configuración de JPA
│   │       ├── repository/  # Consultas y operaciones de persistencia
│   │       └── utils/       # Importación de CSV
│   └── resources/
│       ├── META-INF/persistence.xml
│       └── *.csv
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Modelo de datos

- **Estudiante**: DNI, nombre, apellido, edad, género, ciudad y libreta universitaria (LU). El DNI es la clave primaria.
- **Carrera**: identificador, nombre y duración.
- **Inscripción**: entidad que vincula un estudiante con una carrera y registra año de inscripción, año de graduación y antigüedad. Un estudiante puede tener varias inscripciones y una carrera puede tener muchos estudiantes.

Los diagramas del modelo se encuentran en:

- [Diagrama de clases](src/diagramas/Diagrama%20de%20clase.png)
- [Diagrama entidad-relación (DER)](src/diagramas/DER.png)

## Funcionalidades

El menú de consola permite ejecutar las siguientes operaciones:

1. Dar de alta un estudiante.
2. Matricular un estudiante en una carrera.
3. Listar estudiantes ordenados por apellido ascendente.
4. Buscar un estudiante por LU.
5. Listar estudiantes por género.
6. Listar carreras con inscriptos, ordenadas por cantidad de inscripciones descendente.
7. Buscar estudiantes de una carrera filtrando por ciudad de residencia.
8. Generar un reporte de inscriptos y egresados por año para cada carrera. Las carreras se presentan alfabéticamente y los años cronológicamente.

## Tecnologías

- **JAVA 21** (Lenguaje de programación principal JDK 21)
- **JPA / Hibernate** (API para implementar mapeo objeto-relacional ORM)
- **JPQL** (Lenguaje de consultas de persistencia en Java (JPA))
- **MySQL** (Base de datos relacional)
- **Docker** (Contenedor de la base de datos)
- **Maven** (Getión de dependencias)
- **Lombok** (Reducción de código)

## Requisitos

- Tener instalado [Docker](https://www.docker.com/).
- Tener instalado [Java JDK 17+](https://adoptium.net/).
- Agregar las dependencias de [Maven](https://mvnrepository.com/)

La configuración actual de JPA se conecta a `localhost:3306`, con base de datos `Integrador2`, usuario `root` y contraseña vacía. El archivo [docker-compose.yml](docker-compose.yml) crea MySQL 8 con estos valores para desarrollo local.

## Cómo levantar el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/gonsalomon/TPs-Arquitecturas-Web.git

2.  Levantar la base de datos con Docker 🐳:
    ```bash
    docker-compose up -d

- Si utilizas el IDE Intellij IDEA podes levantarlo aun mas facilmente haciendo click en la seccion de services en el archivo de docker-compose.yml
- Esto inicia un contenedor con MySQL en el puerto 3306.

3. Abrir el proyecto en IntelliJ y ejecutar la clase Main para probar las consultas.

## Configuración de la base de datos
La configuración de la persistencia se encuentra en el archivo `persistence.xml`.  
En este archivo se definen las entidades y las propiedades de conexión a la base de datos MySQL.

### Propiedades principales a configurar:
- **Driver JDBC**: `com.mysql.cj.jdbc.Driver`
- **URL de conexión**: `jdbc:mysql://localhost:3306/Integrador2?createDatabaseIfNotExist=true`
- **Usuario**: `root`
- **Contraseña**: *(vacía por defecto, cambiar según tu entorno)*

Ejemplo de configuración dentro del archivo `persistence.xml`:

    <properties>
            <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver" />
            <property name="jakarta.persistence.jdbc.url"	value="jdbc:mysql://localhost:3306/Integrador2?createDatabaseIfNotExist=true" />
            <property name="jakarta.persistence.jdbc.user" value="root" />
            <property name="jakarta.persistence.jdbc.password" value="" />
    </properties>


## Autores del proyecto
- Cabrera Maria
- Colella Pedro
- Del Valle Klaus
- Reyes Santiago
- Salomon Gonzalo
