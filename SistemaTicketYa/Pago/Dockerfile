# syntax=docker/dockerfile:1
# Microservicio Pago — imagen en dos etapas: se compila con Maven y se ejecuta solo con el JRE.

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# El repositorio local de Maven queda en una cache compartida entre los microservicios:
# las dependencias se bajan una sola vez. "sharing=locked" evita que dos builds lo escriban a la vez.
# Los tests no se ejecutan al armar la imagen (se corren aparte con ./mvnw test).
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q -Dmaven.test.skip=true package

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
