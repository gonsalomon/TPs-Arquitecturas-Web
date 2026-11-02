# Microservicio Evento — búsqueda y cache

## Búsqueda por múltiples criterios (punto 9)

`GET /api/eventos/buscar` — todos los filtros son opcionales y se combinan:

| Parámetro | Ejemplo | Filtra por |
|---|---|---|
| `tipo` | `RECITAL` | tipo de evento |
| `idOrganizador` | `3` | organizador |
| `participante` | `Banda X` | artista, equipo o disertante (contiene, sin mayúsculas) |
| `ciudad` | `Tandil` | ciudad del lugar |
| `desde`, `hasta` | `2026-12-01` | rango de fechas (ambos inclusive) |
| `texto` | `rock` | texto libre en nombre, descripción o participantes |
| `orden` | `FECHA` / `PRECIO` / `RELEVANCIA` | ordenamiento (default `FECHA`) |

- Solo devuelve eventos **PUBLICADO** o **AGOTADO** (los borradores, cancelados y finalizados no se venden).
- **PRECIO**: el más barato primero, según el precio mínimo de sus sectores.
- **RELEVANCIA**: más coincidencias primero (nombre 3 pts, participante 2 pts, descripción 1 pt, artista exacto +2). Empate → el más próximo.

## Cache

Cache en memoria de cada instancia con **Caffeine** (`config/CacheConfig.java`).

| Cache | Qué guarda | TTL | Se invalida cuando… |
|---|---|---|---|
| `eventos` | detalle de un evento (`GET /api/eventos/{id}`) | 10 min | se edita, borra o cambia de estado ese evento, o se edita un lugar |
| `busquedas` | resultados de `/api/eventos/buscar` | 2 min | cualquier evento se edita, borra o cambia de estado, o se edita un lugar |
| `lugares` | detalle de un lugar (por id o nombre) | 30 min | se edita o borra un lugar |

**Por qué:** el catálogo se lee muchísimo más de lo que se escribe, sobre todo en la apertura de un evento masivo, donde miles de clientes consultan el mismo evento. El cache evita ir a Mongo en cada consulta.

**Qué NO se cachea:** la disponibilidad de asientos y cupos. Cambia a cada segundo durante una venta y mostrar un dato viejo haría que un cliente elija un lugar ya vendido. Eso lo resuelve el microservicio de Reservas consultando siempre el dato real.

**Limitación:** al ser un cache local, con varias instancias de Evento cada una puede tener una copia distinta hasta que venza el TTL (por eso los TTL son cortos). Si hiciera falta, el paso siguiente es un cache compartido con Redis.
