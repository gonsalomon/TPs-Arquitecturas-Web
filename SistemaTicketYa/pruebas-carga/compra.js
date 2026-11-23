// =====================================================================================
// TicketYa — prueba de carga del flujo de compra (k6)
//
// Ejecutar con todo el sistema levantado (docker compose up -d --build):
//     docker compose --profile carga run --rm k6
// Con otra carga:
//     USUARIOS=50 COMPRAS_POR_USUARIO=10 docker compose --profile carga run --rm k6
//
// Todo pasa por el API Gateway con JWT, igual que un cliente real.
//
// Escenario 1 "mismo_asiento": todos los usuarios intentan comprar EL MISMO asiento a la vez.
//                              Solo una compra puede resultar exitosa (no doble venta).
// Escenario 2 "compra":        cada usuario compra varias entradas numeradas (crear orden + pagar).
//
// Al terminar escribe informe-prueba-de-carga.md en esta carpeta con los resultados.
// =====================================================================================
import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import exec from 'k6/execution';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const USUARIOS = parseInt(__ENV.USUARIOS || '30', 10);
const COMPRAS = parseInt(__ENV.COMPRAS_POR_USUARIO || '10', 10);
// El sistema limita las entradas por cliente y por evento: se reparten las compras en varios eventos.
const COMPRAS_POR_EVENTO = 4;
const CLAVE = '1234';

const crearOrdenMs = new Trend('crear_orden_ms', true);
const pagarMs = new Trend('pagar_orden_ms', true);
const compraTotalMs = new Trend('compra_completa_ms', true);
const comprasOk = new Counter('compras_confirmadas');
const comprasFallidas = new Counter('compras_fallidas');
const mismoAsientoVendidas = new Counter('mismo_asiento_vendidas');
const mismoAsientoRechazadas = new Counter('mismo_asiento_rechazadas');

export const options = {
  setupTimeout: '10m',
  scenarios: {
    mismo_asiento: {
      executor: 'per-vu-iterations',
      exec: 'mismoAsiento',
      vus: USUARIOS,
      iterations: 1,
      maxDuration: '1m',
    },
    compra: {
      executor: 'per-vu-iterations',
      exec: 'comprar',
      vus: USUARIOS,
      iterations: COMPRAS,
      startTime: '1m',
      maxDuration: '15m',
    },
  },
  thresholds: {
    // No doble venta: de todos los intentos sobre el mismo asiento, exactamente una venta.
    mismo_asiento_vendidas: ['count==1'],
    // Flujo de compra bajo carga
    'http_req_failed{scenario:compra}': ['rate<0.05'],
    'http_req_duration{scenario:compra}': ['p(95)<3000'],
    compra_completa_ms: ['p(95)<5000'],
  },
};

const JSON_H = { 'Content-Type': 'application/json' };

function cuerpo(res) {
  try { return res.json(); } catch (e) { return null; }
}

function crearUsuario(rol, email) {
  const res = http.post(`${BASE}/api/usuarios`, JSON.stringify({
    nombre: 'Carga', apellido: rol, email: email, ciudad: 'Tandil',
    password: CLAVE, authorities: [rol], artistasFavoritos: [],
  }), { headers: JSON_H, tags: { paso: 'preparacion' } });
  const j = cuerpo(res);
  if (res.status >= 300 || !j || j.id === undefined) {
    fail(`No se pudo crear el usuario ${email}: HTTP ${res.status} ${res.body}`);
  }
  return j.id;
}

function login(email) {
  const res = http.post(`${BASE}/api/authenticate`, JSON.stringify({ username: email, password: CLAVE }),
    { headers: JSON_H, tags: { paso: 'preparacion' } });
  const j = cuerpo(res);
  if (res.status >= 300 || !j || !j.id_token) {
    fail(`No se pudo iniciar sesión con ${email}: HTTP ${res.status} ${res.body}`);
  }
  return j.id_token;
}

function autorizado(token, extra) {
  return Object.assign({ 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }, extra || {});
}

// ------------------------------------------------------------------ preparación (no se mide)
export function setup() {
  const corrida = Date.now();
  const filas = USUARIOS + 1;                       // una fila por usuario + una para "mismo asiento"
  const asientosPorFila = Math.max(50, COMPRAS + 1);

  const orgEmail = `org.carga.${corrida}@ticketya.test`;
  const orgId = crearUsuario('ORGANIZADOR', orgEmail);
  const orgToken = login(orgEmail);

  let res = http.post(`${BASE}/api/eventos/lugares`, JSON.stringify({
    nombre: `Estadio Carga ${corrida}`, direccion: 'Av. Carga 100', ciudad: 'Tandil',
    latitud: -37.32, longitud: -59.13,
    sectores: [
      { nombre: 'Platea', tipo: 'NUMERADO', filas: filas, asientosPorFila: asientosPorFila, capacidad: filas * asientosPorFila },
      { nombre: 'Campo', tipo: 'NO_NUMERADO', capacidad: 100000 },
    ],
  }), { headers: autorizado(orgToken), tags: { paso: 'preparacion' } });
  let j = cuerpo(res);
  const lugarId = j && (j.idLugar || j.id);
  if (res.status >= 300 || !lugarId) fail(`No se pudo crear el lugar: HTTP ${res.status} ${res.body}`);

  function crearEventoPublicado(nombre) {
    let r = http.post(`${BASE}/api/eventos?usuarioId=${orgId}`, JSON.stringify({
      nombre: nombre, descripcion: 'Evento para la prueba de carga', tipo: 'RECITAL',
      fechaHora: '2027-12-10T21:00:00', idLugar: lugarId, altaDemanda: false,
      participantes: ['Banda de Carga'],
      precios: [{ sector: 'Platea', precio: 25000 }, { sector: 'Campo', precio: 15000 }],
    }), { headers: autorizado(orgToken), tags: { paso: 'preparacion' } });
    const e = cuerpo(r);
    const id = e && (e.idEvento || e.id);
    if (r.status >= 300 || !id) fail(`No se pudo crear el evento: HTTP ${r.status} ${r.body}`);
    r = http.patch(`${BASE}/api/eventos/${id}/estado?usuarioId=${orgId}`, JSON.stringify({ estado: 'PUBLICADO' }),
      { headers: autorizado(orgToken), tags: { paso: 'preparacion' } });
    if (r.status >= 300) fail(`No se pudo publicar el evento: HTTP ${r.status} ${r.body}`);
    return id;
  }

  const eventos = [];
  const cantidadEventos = Math.ceil(COMPRAS / COMPRAS_POR_EVENTO);
  for (let i = 0; i < cantidadEventos; i++) eventos.push(crearEventoPublicado(`Carga ${corrida} #${i + 1}`));
  const eventoMismoAsiento = crearEventoPublicado(`Carga ${corrida} mismo asiento`);

  const clientes = [];
  for (let i = 0; i < USUARIOS; i++) {
    const email = `cliente.carga.${corrida}.${i}@ticketya.test`;
    const id = crearUsuario('CLIENTE', email);
    clientes.push({ id: id, token: login(email) });
  }
  return { eventos, eventoMismoAsiento, clientes, filaCompartida: filas, inicio: new Date().toISOString() };
}

function clienteDeEsteVU(data) {
  const indice = (exec.vu.idInTest - 1) % data.clientes.length;
  return { indice, cliente: data.clientes[indice] };
}

// ------------------------------------------------------------------ escenario 1: mismo asiento
export function mismoAsiento(data) {
  const { cliente } = clienteDeEsteVU(data);
  const headers = autorizado(cliente.token, { 'X-Usuario-Id': String(cliente.id) });
  // 409/422/400 son la respuesta CORRECTA para quien pierde el asiento: no cuentan como error.
  const esperado = http.expectedStatuses({ min: 200, max: 299 }, 400, 409, 422);

  const res = http.post(`${BASE}/api/ordenes`, JSON.stringify({
    eventoId: data.eventoMismoAsiento, sector: 'Platea',
    asientos: [{ fila: data.filaCompartida, asiento: 1 }],
  }), { headers, responseCallback: esperado, tags: { paso: 'crear_orden' } });

  const orden = cuerpo(res);
  if (res.status >= 300 || !orden || orden.id === undefined) {
    mismoAsientoRechazadas.add(1);
    return;
  }
  const pago = http.post(`${BASE}/api/ordenes/${orden.id}/pago`, JSON.stringify({ medioPago: 'TARJETA_CREDITO' }),
    { headers, responseCallback: esperado, tags: { paso: 'pagar_orden' } });
  const pagada = cuerpo(pago);
  if (pago.status < 300 && pagada && pagada.estado === 'CONFIRMADA') {
    mismoAsientoVendidas.add(1);
  } else {
    mismoAsientoRechazadas.add(1);
  }
}

// ------------------------------------------------------------------ escenario 2: flujo de compra
export function comprar(data) {
  const { indice, cliente } = clienteDeEsteVU(data);
  const iteracion = exec.vu.iterationInScenario;
  const eventoId = data.eventos[Math.floor(iteracion / COMPRAS_POR_EVENTO)];
  const headers = autorizado(cliente.token, { 'X-Usuario-Id': String(cliente.id) });
  const inicio = Date.now();

  // 1. Crear la orden: valida el evento, reserva el asiento por 10 minutos
  const res = http.post(`${BASE}/api/ordenes`, JSON.stringify({
    eventoId: eventoId, sector: 'Platea',
    asientos: [{ fila: indice + 1, asiento: iteracion + 1 }],     // asiento propio de cada usuario
  }), { headers, tags: { paso: 'crear_orden' } });
  crearOrdenMs.add(res.timings.duration);
  const orden = cuerpo(res);
  const creada = check(res, { 'orden creada (2xx)': (r) => r.status >= 200 && r.status < 300 })
    && orden && orden.id !== undefined;
  if (!creada) {
    comprasFallidas.add(1);
    sleep(0.3);
    return;
  }

  // 2. Pagar: cobra en la pasarela simulada, confirma la reserva y emite la entrada con QR
  const pago = http.post(`${BASE}/api/ordenes/${orden.id}/pago`, JSON.stringify({ medioPago: 'TARJETA_CREDITO' }),
    { headers, tags: { paso: 'pagar_orden' } });
  pagarMs.add(pago.timings.duration);
  const pagada = cuerpo(pago);
  const confirmada = check(pago, {
    'pago aceptado (2xx)': (r) => r.status >= 200 && r.status < 300,
    'orden CONFIRMADA con entrada emitida': () => pagada && pagada.estado === 'CONFIRMADA',
  });
  if (confirmada) {
    comprasOk.add(1);
    compraTotalMs.add(Date.now() - inicio);
  } else {
    comprasFallidas.add(1);
  }
  sleep(0.3);    // pausa entre compras de un mismo usuario
}

// ------------------------------------------------------------------ informe
function v(metricas, nombre, campo, defecto) {
  const m = metricas[nombre];
  if (!m || !m.values || m.values[campo] === undefined) return defecto === undefined ? 0 : defecto;
  return m.values[campo];
}
function ms(x) { return `${Math.round(x)} ms`; }
function pct(x) { return `${(x * 100).toFixed(2)} %`; }
function filaTiempos(titulo, metricas, nombre) {
  return `| ${titulo} | ${ms(v(metricas, nombre, 'avg'))} | ${ms(v(metricas, nombre, 'med'))} | ${ms(v(metricas, nombre, 'p(90)'))} | ${ms(v(metricas, nombre, 'p(95)'))} | ${ms(v(metricas, nombre, 'max'))} |`;
}

export function handleSummary(data) {
  const m = data.metrics;
  const ok = v(m, 'compras_confirmadas', 'count');
  const fallidas = v(m, 'compras_fallidas', 'count');
  const intentos = ok + fallidas;
  const vendidas = v(m, 'mismo_asiento_vendidas', 'count');
  const rechazadas = v(m, 'mismo_asiento_rechazadas', 'count');
  const errorCompra = v(m, 'http_req_failed{scenario:compra}', 'rate');
  const p95 = v(m, 'compra_completa_ms', 'p(95)');
  const duracionS = data.state.testRunDurationMs / 1000;
  const duracionCompraS = Math.max(1, duracionS - 60);
  const fecha = new Date().toISOString().replace('T', ' ').substring(0, 16) + ' UTC';

  const umbrales = [];
  for (const nombre of Object.keys(m)) {
    const t = m[nombre].thresholds;
    if (!t) continue;
    for (const expr of Object.keys(t)) umbrales.push(`| \`${nombre}\` | \`${expr}\` | ${t[expr].ok ? 'Cumple' : 'NO cumple'} |`);
  }

  const conclusiones = [];
  conclusiones.push(vendidas === 1
    ? `- **No doble venta verificada bajo carga:** ${USUARIOS} usuarios intentaron comprar el mismo asiento al mismo tiempo y se vendió exactamente una vez; los otros ${rechazadas} intentos fueron rechazados.`
    : `- **Atención:** el asiento disputado se vendió ${vendidas} veces (se esperaba exactamente 1). Revisar el mecanismo contra la doble venta.`);
  conclusiones.push(intentos > 0
    ? `- Se completaron ${ok} de ${intentos} compras (${pct(ok / intentos)}), a un ritmo de ${(ok / duracionCompraS).toFixed(1)} compras por segundo.`
    : '- No se registraron intentos de compra: revisar que el sistema esté levantado.');
  conclusiones.push(`- El 95 % de las compras completas (crear orden + pagar) tardó menos de ${ms(p95)}.`);
  conclusiones.push(v(m, 'pagar_orden_ms', 'avg') > v(m, 'crear_orden_ms', 'avg')
    ? '- El paso más lento es **pagar**: Orden llama en cadena a Pago, Reserva y Entrada de forma síncrona, por lo que su tiempo es la suma de los tres.'
    : '- El paso más lento es **crear la orden**: Orden consulta a Evento y a Reserva antes de responder.');
  conclusiones.push(errorCompra > 0.05
    ? `- La tasa de error del flujo de compra fue ${pct(errorCompra)}, por encima del 5 % tolerado: con esta carga el sistema se degrada.`
    : `- La tasa de error del flujo de compra fue ${pct(errorCompra)}, dentro del 5 % tolerado.`);

  const informe = `# Informe de la prueba de carga — flujo de compra

> Generado automáticamente por \`pruebas-carga/compra.js\` el ${fecha}. Los números de este informe salen de la corrida real.

## Objetivo

Medir el comportamiento del flujo de compra de una entrada numerada con muchos usuarios a la vez y verificar que, bajo carga, un mismo asiento no se vende dos veces.

## Cómo se ejecutó

- **Herramienta:** k6 (contenedor \`grafana/k6\`), dentro de la misma red de Docker que el sistema.
- **Entorno:** el sistema completo levantado con \`docker compose up -d --build\` en una sola máquina (9 microservicios, 8 MySQL, 1 MongoDB). _Completar: procesador y memoria del equipo._
- **Punto de entrada:** todas las peticiones pasan por el API Gateway con token JWT, como un cliente real.
- **Datos de prueba:** la preparación crea un organizador, un lugar, los eventos publicados y un cliente con su token por cada usuario concurrente. Ese tiempo no se mide.

| Parámetro | Valor |
|---|---|
| Usuarios concurrentes | ${USUARIOS} |
| Compras por usuario | ${COMPRAS} |
| Compras intentadas | ${intentos} |
| Duración total de la corrida | ${duracionS.toFixed(0)} s |

### Escenarios

1. **Mismo asiento.** Los ${USUARIOS} usuarios piden a la vez el mismo asiento de un mismo evento. Resultado esperado: una sola venta.
2. **Flujo de compra.** Cada usuario repite ${COMPRAS} veces: \`POST /api/ordenes\` (valida el evento y reserva el asiento) y \`POST /api/ordenes/{id}/pago\` (cobra, confirma la reserva y emite la entrada con QR). Cada usuario compra asientos distintos, con una pausa de 0,3 s entre compras.

## Resultados

### No doble venta (escenario 1)

| Intentos sobre el mismo asiento | Ventas | Rechazos |
|---|---|---|
| ${vendidas + rechazadas} | **${vendidas}** | ${rechazadas} |

### Flujo de compra (escenario 2)

| Compras confirmadas | Compras fallidas | Éxito | Compras por segundo | Errores HTTP |
|---|---|---|---|---|
| ${ok} | ${fallidas} | ${intentos > 0 ? pct(ok / intentos) : '-'} | ${(ok / duracionCompraS).toFixed(1)} | ${pct(errorCompra)} |

### Tiempos de respuesta

| Operación | Promedio | Mediana | p90 | p95 | Máximo |
|---|---|---|---|---|---|
${filaTiempos('Crear orden', m, 'crear_orden_ms')}
${filaTiempos('Pagar orden', m, 'pagar_orden_ms')}
${filaTiempos('Compra completa', m, 'compra_completa_ms')}
${filaTiempos('Todas las peticiones HTTP', m, 'http_req_duration')}

Peticiones HTTP totales (incluye la preparación): ${v(m, 'http_reqs', 'count')}.

### Umbrales

| Métrica | Condición | Resultado |
|---|---|---|
${umbrales.join('\n')}

## Conclusiones

${conclusiones.join('\n')}

## Limitaciones

- Todo corre en una sola máquina, incluido el generador de carga: los tiempos son orientativos y no representan un despliegue real.
- La pasarela de pagos es simulada y responde al instante; con una pasarela real el paso de pago sería más lento.
- No se probó la fila virtual: el evento de la prueba no es de alta demanda.

## Cómo reproducir

\`\`\`bash
docker compose up -d --build
docker compose --profile carga run --rm k6
# con otra carga:
USUARIOS=50 COMPRAS_POR_USUARIO=10 docker compose --profile carga run --rm k6
\`\`\`
`;

  const consola = `
================ TicketYa — prueba de carga ================
Usuarios concurrentes : ${USUARIOS}
Mismo asiento         : ${vendidas} venta(s), ${rechazadas} rechazo(s)  ${vendidas === 1 ? '[OK]' : '[REVISAR]'}
Compras confirmadas   : ${ok} de ${intentos}
Errores HTTP (compra) : ${pct(errorCompra)}
Compra completa       : promedio ${ms(v(m, 'compra_completa_ms', 'avg'))}, p95 ${ms(p95)}
Informe               : pruebas-carga/informe-prueba-de-carga.md
============================================================
`;
  return { 'informe-prueba-de-carga.md': informe, stdout: consola };
}
