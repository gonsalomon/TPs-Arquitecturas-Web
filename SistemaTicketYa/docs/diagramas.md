# TicketYa — Diagramas de arquitectura

Los diagramas están en [Mermaid](https://mermaid.js.org/): GitHub los muestra como imagen directamente.
Todo lo de este documento surge del código de la rama `develop` (entidades, clientes Feign y `application.yml` de cada microservicio).

- [1. Diagrama de módulos](#1-diagrama-de-módulos)
- [2. Diagrama de secuencia — compra de una entrada numerada](#2-diagrama-de-secuencia--compra-de-una-entrada-numerada)
- [3. Diagramas entidad-relación por microservicio](#3-diagramas-entidad-relación-por-microservicio)
- [4. Relaciones entre servicios y entidades solapadas](#4-relaciones-entre-servicios-y-entidades-solapadas)

---

## 1. Diagrama de módulos

```mermaid
flowchart LR
    C([Cliente / Organizador / Admin<br/>Postman · Frontend])

    subgraph GW[API Gateway :8080]
        G[Spring Cloud Gateway<br/>ruteo por path /api/**]
    end

    subgraph US[Usuario :8081]
        U[Usuarios y roles]
    end
    subgraph EV[Evento :8083]
        E[Eventos · Lugares · Sectores<br/>búsqueda + cache Caffeine]
    end
    subgraph OR[Orden :8087]
        O[Orquesta la compra<br/>límite por cliente · vencimiento]
    end
    subgraph RE[Reserva :8084]
        R[Bloqueo temporal de asientos<br/>vence a los 10 min]
    end
    subgraph PA[Pago :8082]
        P[Pago simulado · reembolsos]
    end
    subgraph EN[Entrada :8085]
        N[Emisión con QR · validez<br/>devoluciones / reintegros]
    end
    subgraph NO[Notificación :8086]
        NT[Avisos por email / push simulados]
    end

    DBU[(MySQL usuarios<br/>:3303)]
    DBE[(MongoDB eventos<br/>:27018)]
    DBO[(MySQL ordenes<br/>:3309)]
    DBR[(MySQL reservas<br/>:3306)]
    DBP[(MySQL pagos<br/>:3304)]
    DBN[(MySQL entradas<br/>:3307)]
    DBT[(MySQL notificaciones<br/>:3308)]

    C -->|HTTP REST| G
    G --> U & E & O & R & P & N & NT

    E -.->|Feign: valida rol| U
    O -.->|Feign: evento, lugar, sectores| E
    O -.->|Feign: crear / confirmar / liberar| R
    O -.->|Feign: cobrar| P
    O -.->|Feign: emitir entradas| N
    R -.->|Feign: evento y lugar| E
    N -.->|Feign: cliente| U
    N -.->|Feign: reembolso| P
    N -.->|Feign: liberar reserva| R

    U --- DBU
    E --- DBE
    O --- DBO
    R --- DBR
    P --- DBP
    N --- DBN
    NT --- DBT
```

| Módulo | Responsabilidad | Base | Puerto |
|---|---|---|---|
| Gateway | Punto de entrada único, ruteo a cada servicio (JWT en la Entrega 2) | — | 8080 |
| Usuario | Alta, baja y modificación de usuarios y roles | MySQL `usuarios` | 8081 |
| Evento | Lugares con sectores, eventos y su ciclo de vida, búsqueda por criterios, cache del catálogo | MongoDB `eventos` | 8083 |
| Orden | Orquesta la compra: valida evento y límite, reserva, cobra y emite; vence órdenes impagas | MySQL `ordenes` | 8087 |
| Reserva | Bloquea asientos mientras se paga; vence a los 10 minutos | MySQL `reservas` | 8084 |
| Pago | Pago simulado y reembolsos | MySQL `pagos` | 8082 |
| Entrada | Emite entradas con QR, controla que un asiento no se emita dos veces, devoluciones y reintegros | MySQL `entradas` | 8085 |
| Notificación | Registra y "envía" avisos (simulado) | MySQL `notificaciones` | 8086 |

**Comunicación:** síncrona por REST. Entre servicios se usa OpenFeign (líneas punteadas). Cada microservicio tiene **su propia base** y nadie lee la base de otro: los datos ajenos se piden por API.

**Fila virtual** todavía no está implementada (Entrega 2).

---

## 2. Diagrama de secuencia — compra de una entrada numerada

Incluye los casos de **pago rechazado** y **reserva vencida**. Está en el diseño del flujo de compra:
[`openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md` → sección 11](../openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md#11-diagrama-de-secuencia--compra-de-una-entrada-numerada).

---

## 3. Diagramas entidad-relación por microservicio

`PK` = clave primaria · `UK` = única · `"ref X"` = id de otro microservicio (no hay FK real entre bases).

### Usuario — MySQL `usuarios`

```mermaid
erDiagram
    USUARIO {
        bigint id_usuario PK
        varchar nombre
        varchar apellido
        varchar email "unico (validado en el servicio)"
        enum rol "ADMINISTRADOR, ORGANIZADOR, CLIENTE, PERSONALACCESO"
    }
```

### Evento — MongoDB `eventos`

En Mongo los sectores y los precios van **embebidos** en el documento (no son colecciones aparte).

```mermaid
erDiagram
    LUGAR ||--|{ SECTOR : "embebe"
    LUGAR ||--o{ EVENTO : "se realiza en (idLugar)"
    EVENTO ||--o{ PRECIO_SECTOR : "embebe"
    SECTOR ||--o{ PRECIO_SECTOR : "precio por evento (por nombre)"

    LUGAR {
        ObjectId idLugar PK
        string nombre UK
        string direccion
        string ciudad
        double latitud
        double longitud
    }
    SECTOR {
        string nombre "unico dentro del lugar"
        enum tipo "NUMERADO, NO_NUMERADO"
        int filas "solo numerado"
        int asientosPorFila "solo numerado"
        int capacidad "solo no numerado"
    }
    EVENTO {
        ObjectId idEvento PK
        string nombre
        string descripcion
        enum tipo "RECITAL, TEATRO, DEPORTE, CONFERENCIA, OTRO"
        bigint idOrganizador "ref Usuario"
        string_array participantes
        datetime fechaHora
        ObjectId idLugar "ref LUGAR"
        enum estado "BORRADOR, PUBLICADO, AGOTADO, SUSPENDIDO, CANCELADO, FINALIZADO"
    }
    PRECIO_SECTOR {
        string sector "nombre del SECTOR"
        double precio
    }
```

### Orden — MySQL `ordenes`

```mermaid
erDiagram
    ORDEN ||--|{ ITEM_ORDEN : "contiene"

    ORDEN {
        bigint id_orden PK
        bigint cliente_id "ref Usuario"
        varchar evento_id "ref Evento"
        varchar sector "nombre del sector en Evento"
        datetime fecha_evento "copia de Evento"
        enum estado "PENDIENTE_PAGO, PAGO_APROBADO, CONFIRMADA, RECHAZADA, VENCIDA"
        decimal total
        enum medio_pago
        bigint id_reserva "ref Reserva"
        bigint id_pago "ref Pago"
        datetime fecha_creacion
        datetime fecha_vencimiento
        datetime fecha_actualizacion
        bigint version "bloqueo optimista"
    }
    ITEM_ORDEN {
        bigint id_item PK
        bigint id_orden FK
        int fila
        int asiento
        int numero_asiento "numero que usa Reserva"
        decimal precio_unitario "copia de Evento"
        varchar clave_asiento_activo UK "evento|sector|fila|asiento mientras la orden esta activa"
    }
```

### Reserva — MySQL `reservas`

```mermaid
erDiagram
    RESERVA ||--o{ RESERVA_ASIENTOS : "bloquea"

    RESERVA {
        bigint id_reserva PK
        bigint id_cliente "ref Usuario"
        varchar id_evento "ref Evento"
        varchar sector "nombre del sector en Evento"
        float monto
        int cantidad
        enum estado "PENDIENTE, CONFIRMADA, VENCIDA, CANCELADA, LIBERADA"
        datetime fecha_inicio
        datetime fecha_vencimiento "inicio + 10 min"
    }
    RESERVA_ASIENTOS {
        bigint reserva_id FK
        bigint asiento "numero de asiento"
    }
```

### Pago — MySQL `pagos`

```mermaid
erDiagram
    PAGO {
        bigint id_pago PK
        bigint id_reserva "ref Reserva (un pago por reserva)"
        bigint id_cliente "ref Usuario"
        enum estado "ACEPTADO, RECHAZADO, REEMBOLSADO"
        enum metodo_pago "TARJETA_CREDITO, TARJETA_DEBITO, BILLETERA_VIRTUAL"
        decimal monto
        datetime fecha_emision
        datetime fecha_reembolso
    }
```

### Entrada — MySQL `entradas`

```mermaid
erDiagram
    ENTRADA {
        bigint id PK
        varchar evento_id "ref Evento"
        varchar sector_id "nombre del sector en Evento"
        bigint cliente_id "ref Usuario"
        bigint orden_id "ref Orden"
        int fila
        int asiento
        decimal precio "copia de Orden"
        datetime fecha_evento "copia de Evento (regla de 72 h)"
        enum estado "VALIDA, USADA, REINTEGRO"
        varchar codigo_qr UK
        varchar clave_idempotencia UK "no emitir dos veces la misma orden"
        varchar clave_asiento_activo UK "un asiento no se emite dos veces"
        bigint version "bloqueo optimista"
    }
```

### Notificación — MySQL `notificaciones`

```mermaid
erDiagram
    NOTIFICACION {
        bigint id PK
        enum tipo "CONFIRMACION_COMPRA, CANCELACION_EVENTO, MODIFICACION_EVENTO, EVENTO_ARTISTA_FAVORITO"
        enum canal "EMAIL, PUSH"
        varchar email "copia de Usuario"
        varchar estado
    }
```

---

## 4. Relaciones entre servicios y entidades solapadas

No hay claves foráneas entre bases: cada servicio guarda **el id** de la entidad de otro servicio y la consulta por API cuando la necesita.

```mermaid
flowchart LR
    USUARIO["Usuario.idUsuario"]
    EVENTO["Evento.idEvento"]
    LUGAR["Lugar.idLugar / Sector.nombre"]
    RESERVA["Reserva.idReserva"]
    PAGO["Pago.idPago"]
    ORDEN["Orden.idOrden"]

    EVENTO -- idOrganizador --> USUARIO
    EVENTO -- idLugar --> LUGAR
    ORDEN -- clienteId --> USUARIO
    ORDEN -- eventoId --> EVENTO
    ORDEN -- sector --> LUGAR
    ORDEN -- idReserva --> RESERVA
    ORDEN -- idPago --> PAGO
    RESERVA -- idCliente --> USUARIO
    RESERVA -- idEvento --> EVENTO
    PAGO -- idReserva --> RESERVA
    PAGO -- idCliente --> USUARIO
    ENTRADA["Entrada.id"] -- ordenId --> ORDEN
    ENTRADA -- clienteId --> USUARIO
    ENTRADA -- eventoId --> EVENTO
```

| Referencia | Guardada en | Apunta a |
|---|---|---|
| id de cliente / organizador | Evento (`idOrganizador`), Orden (`clienteId`), Reserva (`idCliente`), Pago (`idCliente`), Entrada (`clienteId`) | Usuario |
| id de evento | Orden, Reserva, Entrada | Evento (ObjectId de Mongo, por eso es `String`) |
| sector | Orden (`sector`), Reserva (`sector`), Entrada (`sectorId`) | Nombre del sector dentro del Lugar (no tiene id propio) |
| id de reserva | Orden, Pago | Reserva |
| id de pago | Orden | Pago |
| id de orden | Entrada | Orden |

### Entidades solapadas

Hay información que se **repite a propósito** en varias bases para que cada servicio funcione sin consultar a otro en cada operación:

| Dato | Dónde se repite | Fuente de verdad | Por qué |
|---|---|---|---|
| **Asiento ocupado** (evento + sector + fila/asiento) | Reserva (`reserva_asientos`), Orden (`item_orden.clave_asiento_activo`), Entrada (`clave_asiento_activo`) | **Entrada** para lo vendido; Reserva para el bloqueo temporal | Defensa en profundidad contra la doble venta: cada servicio tiene una restricción `UNIQUE` en su base |
| **Precio** | Evento (`PrecioSector`) → Orden (`precio_unitario`, `total`) → Pago (`monto`) → Entrada (`precio`) | Evento al momento de comprar | Se congela el precio pagado aunque el organizador lo cambie después |
| **Fecha del evento** | Evento → Orden (`fecha_evento`) → Entrada (`fecha_evento`) | Evento | Entrada controla la regla de 72 h de devolución sin llamar a Evento |
| **Email del usuario** | Usuario → Notificación (`email`) | Usuario | La notificación queda registrada con el destinatario de ese momento |

**Riesgo del solapamiento:** si un dato cambia en la fuente (por ejemplo, se reprograma la fecha de un evento) las copias quedan desactualizadas. Para la Entrega 2 se propone propagar esos cambios con eventos asíncronos.

**Inconsistencias de nombres a unificar:** el cliente se llama `idCliente` en Reserva y Pago, pero `clienteId` en Orden y Entrada; el sector se llama `sector` en Orden y Reserva, y `sectorId` en Entrada.
