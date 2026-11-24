# Guía reproducible de demostración — Virtual Insurance

**Fecha de preparación:** 23 de noviembre de 2026  
**Entorno:** ejecución local con Oracle Database 26ai, Spring Boot y React/Vite

Esta guía prepara y recorre una demostración utilizando exclusivamente cuentas, tarjetas y operaciones ficticias. No ingreses datos personales, fiscales o bancarios reales.

## 1. Requisitos

- Oracle Database 26ai con la PDB `ETOBDA_S1` disponible en `localhost:1522`.
- Esquema `VI_APP` creado mediante los scripts de `database/admin/` y `database/schema/`.
- JDK 21 o posterior y Node.js/npm instalados.
- Variables `VI_DB_PASSWORD` y `VI_CARD_ENCRYPTION_KEY` disponibles para Spring Boot.

## 2. Restaurar los datos ficticios

Conecta SQL Developer como `VI_APP@ETOBDA_S1` y ejecuta mediante **Run Script**, en este orden:

```text
database/seed/01_catalogos.sql
database/seed/02_clientes.sql
database/seed/03_cotizaciones.sql
database/seed/04_instrumentos_pago.sql
database/seed/05_usuarios_internos.sql
```

Los scripts usan `merge` o actualizaciones controladas y pueden repetirse. `04_instrumentos_pago.sql` restablece saldos y estados de los instrumentos ficticios.

## 3. Iniciar backend y frontend

Terminal del backend:

```bash
cd backend
read -s "VI_DB_PASSWORD?Contraseña de VI_APP: "
export VI_DB_PASSWORD
echo
export VI_CARD_ENCRYPTION_KEY="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

Terminal del frontend:

```bash
cd frontend
npm install
npm run dev
```

Comprobaciones iniciales:

```text
Backend:  http://localhost:8080/actuator/health
Frontend: http://localhost:5173
```

## 4. Credenciales ficticias

Cliente:

```text
emmanuel.demo@virtual-insurance.test
Virtual2026!
```

Administración de cancelaciones:

```text
admin.cancelaciones@virtual-insurance.test
Virtual2026!
```

El acceso administrativo se encuentra en `http://localhost:5173/admin/acceso`.

## 5. Instrumentos de pago para demostración

Todos usan vencimiento `12/30`. Visa y Mastercard usan CVV `123`; American Express usa `1234`.

| Escenario | Red | Número ficticio | Resultado esperado |
|---|---|---|---|
| Aprobación Visa | Visa | `4242 4242 4242 1111` | Pago aprobado y póliza emitida |
| Fondos insuficientes | Visa | `4000 0000 0000 2222` | Intento rechazado, sin póliza |
| Tarjeta bloqueada | Visa | `4000 0000 0000 3333` | Rechazo controlado |
| Rechazo del procesador | Visa | `4000 0000 0000 5555` | Rechazo controlado |
| Aprobación Mastercard | Mastercard | `5105 1051 0510 5100` | Pago aprobado y póliza emitida |
| Aprobación American Express | American Express | `3714 4963 5398 431` | Pago aprobado y póliza emitida |

## 6. Recorrido principal

1. Abrir `/acceso` e iniciar sesión con el cliente ficticio.
2. Verificar Resumen, sidebar de escritorio y actividad limitada a tres movimientos.
3. Abrir Cotizaciones, Pólizas y Pagos; probar búsqueda y confirmar que se limpia al cambiar de módulo.
4. Crear una cotización de auto aprobada y revisar la propuesta.
5. Continuar a pago con la Visa aprobada y confirmar la emisión de una sola póliza.
6. Descargar la póliza y el comprobante PDF.
7. Solicitar cancelación con fecha efectiva fija al día actual.
8. Cerrar sesión de cliente y entrar a `/admin/acceso`.
9. Localizar la solicitud, abrirla y aprobarla o rechazarla con observación.
10. Volver al cliente y comprobar el resultado en panel, detalle de póliza y PDF regenerado.

## 7. Recorridos negativos recomendados

- Registro con campos vacíos, RFC inválido y contraseñas distintas.
- Escritura y pegado de números en nombres y letras en campos numéricos.
- Cotización rechazada que no permite contratar.
- Pago con fondos insuficientes que no emite póliza.
- Segundo pago sobre una cotización ya contratada.
- Segunda solicitud de cancelación sobre una póliza no activa.
- Acceso directo a una ruta protegida después de cerrar sesión.

## 8. Revisión responsiva final

Repetir la navegación con un ancho menor a 1024 px:

- aparece la topbar azul con isotipo y botón de tres barras;
- no aparece la sidebar fija;
- el drawer cierra al seleccionar una ruta, tocar el fondo y presionar Escape;
- formularios, perfil, historiales, footer y documentos no generan desplazamiento horizontal involuntario.

## 9. Cierre de la demostración

- Cerrar las sesiones de cliente y administración.
- No conservar capturas con contraseñas, cookies, tokens CSRF o secretos de entorno.
- Volver a ejecutar `database/seed/04_instrumentos_pago.sql` si se requiere restaurar los saldos para otra demostración.
