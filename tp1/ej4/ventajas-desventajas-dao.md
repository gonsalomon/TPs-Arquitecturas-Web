# Ejercicio 4 — Ventajas y desventajas del DAO (Ejercicio 3) frente a los Ejercicios 1 y 2

## Ventajas

- **Independencia del motor de base de datos**: el `PersonaService` y el `Main` no saben si atrás
  hay Derby o MySQL, solo conocen la interfaz `PersonaDAO`. En los Ejercicios 1 y 2 había que
  reescribir (o duplicar) toda la lógica de conexión y consultas para cada motor.
- **Cambiar de base de datos es una línea**: pasar de Derby a MySQL es reemplazar
  `new PersonaDAODerby()` por `new PersonaDAOMySQL()`, sin tocar el resto del programa.
- **Las consultas SQL quedan centralizadas**: cada DAO concentra sus propias sentencias SQL,
  en vez de estar repetidas (y potencialmente desactualizadas entre sí) en distintas clases,
  como pasaba con `DerbyPersonaApp` y `MySQLPersonaApp`.
- **Más fácil de testear**: se podría escribir un `PersonaDAO` "falso" en memoria (sin base de
  datos real) para probar `PersonaService` de forma aislada y rápida.

## Desventajas

- **Más archivos y capas para un caso tan chico**: para algo tan simple como esto (una tabla,
  tres operaciones), pasar de dos clases sueltas a una interfaz + dos implementaciones + un
  service se siente sobre-diseñado.
- **El mapeo ResultSet → objeto sigue siendo manual**: cada DAO repite a mano la lógica de leer
  columna por columna y armar un `Persona`. Con más entidades esto se vuelve repetitivo — es
  justo lo que después resuelve JPA/Hibernate (Ejercicios 5 y 6).
- **El manejo de conexiones sigue siendo básico**: cada método abre y cierra su propia conexión,
  no hay pool de conexiones ni transacciones compartidas entre varias operaciones del mismo DAO.
