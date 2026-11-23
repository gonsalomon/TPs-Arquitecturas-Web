# Informe de la prueba de carga — flujo de compra

> **Pendiente de ejecución.** Este archivo se reemplaza solo, con los resultados reales, al correr la prueba:
>
> ```bash
> docker compose up -d --build
> docker compose --profile carga run --rm k6
> ```

El informe generado incluye: usuarios concurrentes, compras confirmadas y fallidas, tiempos de respuesta (promedio, mediana, p90, p95 y máximo) de crear la orden, pagarla y de la compra completa, tasa de errores, la verificación de que un mismo asiento disputado por todos los usuarios se vende una sola vez, y las conclusiones.

El escenario está descripto en [`compra.js`](compra.js).
