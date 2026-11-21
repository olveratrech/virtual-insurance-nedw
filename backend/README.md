# Backend de Virtual Insurance

API REST de Virtual Insurance construida con Spring Boot, Spring Security y Oracle.

## Requisitos

- JDK 21 o una versión posterior capaz de compilar para Java 21.
- Oracle Database accesible desde macOS.
- Esquema `VI_APP` desplegado en la PDB `ETOBDA_S1`.

El proyecto incluye Maven Wrapper, por lo que no requiere una instalación global específica de Maven.

## Configuración local segura

La aplicación obtiene la conexión mediante variables de entorno:

| Variable | Obligatoria | Valor predeterminado |
|---|---:|---|
| `VI_DB_URL` | No | `jdbc:oracle:thin:@//localhost:1522/etobda_s1.fi.unam` |
| `VI_DB_USERNAME` | No | `VI_APP` |
| `VI_DB_PASSWORD` | Sí | Ninguno |
| `VI_CARD_ENCRYPTION_KEY` | Sí para procesar pagos | Ninguno |

`.env.example` documenta estas variables, pero Spring Boot no carga archivos `.env` automáticamente. No guardes una contraseña real en ese archivo ni en `application.properties`.

En macOS con Zsh, captura la contraseña sin mostrarla ni escribirla en el historial:

```bash
cd backend
read -s "VI_DB_PASSWORD?Contraseña de VI_APP: "
export VI_DB_PASSWORD
echo
```

Genera una clave local AES-256 para los números exclusivamente ficticios y expórtala en la misma terminal:

```bash
export VI_CARD_ENCRYPTION_KEY="$(openssl rand -base64 32)"
```

No cambies la clave mientras necesites descifrar tarjetas guardadas y nunca la agregues al repositorio. El CVV no se almacena.

Si la URL o el usuario cambian, expórtalos también:

```bash
export VI_DB_URL='jdbc:oracle:thin:@//localhost:1522/etobda_s1.fi.unam'
export VI_DB_USERNAME='VI_APP'
```

Las variables exportadas solo existen en esa terminal y en los procesos iniciados desde ella.

## Ejecutar

```bash
./mvnw spring-boot:run
```

La comprobación de salud permitida públicamente es:

```bash
curl -i http://localhost:8080/actuator/health
```

El resultado esperado es HTTP `200` con un cuerpo equivalente a:

```json
{"status":"UP"}
```

Actuator no expone detalles de la conexión. Mientras se implementa la autenticación, las demás rutas permanecen denegadas.

## Catálogo público de géneros

El primer recorrido vertical consulta el catálogo real de Oracle:

```bash
curl -i http://localhost:8080/catalogos/generos
```

La respuesta utiliza el contrato `{ "items": [...] }`. Solo `GET` es público; cualquier otro método o ruta continúa denegado hasta implementar la autenticación.

## Registro y sesión

El registro utiliza `multipart/form-data` y requiere un token CSRF obtenido previamente:

```text
GET  /auth/csrf
POST /auth/registro
POST /auth/login
GET  /auth/me
POST /auth/logout
```

La parte `data` contiene el JSON de registro con `Content-Type: application/json`. La parte opcional `photo` puede contener un JPEG o PNG de hasta 2 MiB; si se omite, Oracle conserva `FOTO` como `NULL` y `photoUrl` se devuelve como `null`. Un registro correcto crea una sesión mediante cookie `JSESSIONID` con `HttpOnly` y `SameSite=Lax`.

`GET /auth/me` responde `200` con la cookie de sesión y `401` sin ella. La URL `/clientes/me/foto` ya forma parte de las respuestas, pero su descarga se implementará junto con los recursos protegidos del cliente.

El login recibe JSON con `email` y `password`. Normaliza el correo, verifica el hash BCrypt almacenado y devuelve el mismo problema `401 INVALID_CREDENTIALS` tanto para un correo inexistente como para una contraseña incorrecta. Un login correcto crea una sesión autenticada.

Login y logout requieren el encabezado CSRF. `POST /auth/logout` invalida la sesión, elimina la cookie `JSESSIONID` y responde `204` sin cuerpo.

## Compilar y probar

```bash
./mvnw clean test
./mvnw package
```

Las pruebas que cargan el contexto requieren que `VI_DB_PASSWORD` esté disponible y que Oracle sea accesible.

El nombre, tipo, alcance y último resultado confirmado de cada prueba se encuentran en el [registro consolidado de pruebas](../docs/PRUEBAS.md).

## Cotizaciones

Las rutas de cotización requieren sesión y CSRF para las operaciones de escritura:

```text
POST /cotizaciones/auto
POST /cotizaciones/medicas
GET  /cotizaciones/{quoteId}
```

Las reglas de auto y médico se encuentran en estrategias separadas. Una creación persiste la cotización, su único subtipo y el historial inicial dentro de una transacción. La consulta combina `quoteId` con el cliente autenticado y responde `404` cuando el recurso no existe o pertenece a otra cuenta.

Los costos son valores académicos fijos por producto; no representan un cálculo actuarial ni una oferta comercial real.

## Pasarela académica de pagos

`POST /cotizaciones/{quoteId}/intentos-pago` requiere sesión, CSRF e `Idempotency-Key`. La pasarela usa exclusivamente los instrumentos de `database/seed/04_instrumentos_pago.sql`, bloquea el registro durante el débito y emite intento, póliza, historial y documentos dentro de una sola transacción.

Un rechazo de negocio también responde `201` y conserva el intento, pero no descuenta saldo ni emite póliza. Repetir una clave de idempotencia devuelve el resultado previo sin un segundo cargo.

## Consulta de pólizas

`GET /polizas/{policyId}` requiere sesión y combina la póliza con su cotización, cliente, último pago y metadatos de documentos. La consulta siempre incluye `policyId + clientId`; una póliza inexistente o ajena responde el mismo `404 POLICY_NOT_FOUND`.

La respuesta no incluye BLOB, factura JSON/XML, número de tarjeta ni CVV. Los documentos exponen solamente identificador, tipo, nombre y una ruta de descarga protegida por sesión y propiedad.

## Documentos PDF

`GET /polizas/{policyId}/documentos/{documentId}/contenido` entrega únicamente documentos pertenecientes al cliente autenticado. Responde `application/pdf`, `Content-Disposition: attachment` y `Cache-Control: no-store`.

Apache PDFBox genera una póliza contractual académica y un comprobante de pago real. Los datos variables provienen de Oracle y los textos editables se concentran en `AcademicContractContent`; su versión inicial es `NEDW-2026.1`. Cada descarga regenera y actualiza el BLOB, por lo que las pólizas anteriores adoptan la plantilla vigente sin exponer una operación destructiva.
