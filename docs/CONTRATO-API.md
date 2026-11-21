# Contrato de API REST de Virtual Insurance

**Versión:** 1.1

**Fecha:** 21 de noviembre de 2026

**Rutas del backend:** desde `/`

**Proxy local de React:** prefijo `/api`, eliminado por Vite antes de reenviar a Spring

**Consumidor inicial:** frontend React de Virtual Insurance

## 1. Objetivo

Este documento define el límite vigente entre React y Spring Boot. Los nombres de la API no dependen de los nombres físicos de Oracle: los DTO usan `camelCase` en inglés y el esquema conserva los nombres acordados en español.

## 2. Convenciones generales

### Formatos

- JSON: `application/json`.
- Errores: `application/problem+json`.
- Registro con fotografía opcional: `multipart/form-data`.
- Fechas: `YYYY-MM-DD`.
- Fecha y hora: ISO 8601 con zona, por ejemplo `2026-11-19T14:30:00-06:00`.
- Importes: números JSON, sin `$`, comas ni texto de moneda.
- Moneda de esta versión: `MXN`.
- Identificadores: números enteros positivos.
- Claves de catálogo y estados: mayúsculas con guion bajo.
- Campos desconocidos en una petición: se rechazan con `400 Bad Request`.

### Autenticación

- Spring Security administrará una sesión de servidor.
- El navegador recibirá una cookie de sesión `HttpOnly`.
- La cookie no será accesible desde React.
- Las operaciones protegidas requieren una sesión válida.
- La sesión contiene una sola identidad: cliente o usuario interno. Un nuevo login reemplaza la identidad anterior de esa sesión.
- Las rutas comerciales requieren `ROLE_CLIENT`; las rutas `/admin/**` requieren `ROLE_ADMIN` o `ROLE_CANCELLATION_REVIEWER`.
- Durante desarrollo, Vite redirigirá `/api` a Spring Boot para trabajar bajo el mismo origen lógico.
- Las peticiones que modifican estado estarán protegidas contra CSRF.
- Ninguna contraseña, número de tarjeta o CVV aparecerá en respuestas, errores o registros.

### Códigos HTTP

| Código | Uso |
|---|---|
| `200 OK` | Consulta o operación completada |
| `201 Created` | Recurso creado, incluso un intento de pago rechazado por negocio |
| `204 No Content` | Cierre de sesión exitoso |
| `400 Bad Request` | JSON, archivo o formato inválido |
| `401 Unauthorized` | No existe una sesión válida |
| `403 Forbidden` | Sesión válida sin acceso al recurso o CSRF inválido |
| `404 Not Found` | Recurso inexistente o no accesible para el cliente |
| `409 Conflict` | Conflicto de estado o duplicidad |
| `422 Unprocessable Content` | Validación de campos o regla de negocio |
| `500 Internal Server Error` | Error no controlado; no expone detalles internos |

## 3. Respuesta de error

Todos los errores controlados usan la misma estructura:

```json
{
  "type": "https://virtual-insurance.local/problems/validation-error",
  "title": "La solicitud contiene datos inválidos",
  "status": 422,
  "detail": "Corrige los campos indicados.",
  "instance": "/cotizaciones/auto",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-11-19T14:30:00-06:00",
  "fieldErrors": [
    {
      "field": "driverAge",
      "message": "La edad debe estar entre 18 y 90 años."
    }
  ]
}
```

`fieldErrors` solamente aparece cuando el error corresponde a campos concretos.

Códigos de negocio previstos:

- `EMAIL_ALREADY_EXISTS`
- `FISCAL_ID_ALREADY_EXISTS`
- `INVALID_CREDENTIALS`
- `INVALID_ADMIN_CREDENTIALS`
- `INVALID_ADMIN_SESSION`
- `VALIDATION_ERROR`
- `RESOURCE_NOT_FOUND`
- `RESOURCE_NOT_OWNED`
- `QUOTE_NOT_APPROVED`
- `QUOTE_EXPIRED`
- `QUOTE_ALREADY_PAID`
- `INVALID_IDEMPOTENCY_KEY`
- `TEST_CARD_NOT_FOUND`
- `INSUFFICIENT_FUNDS`
- `EXPIRED_CARD`
- `BLOCKED_CARD`
- `INVALID_SECURITY_CODE`
- `PROCESSOR_DECLINED`
- `POLICY_NOT_ACTIVE`
- `CANCELLATION_ALREADY_PENDING`
- `UNSUPPORTED_FILE`

Para no revelar la existencia de recursos de otros usuarios, una póliza o cotización ajena responde `404`, no `403`.

## 4. Catálogos públicos

### `GET /catalogos/generos`

No requiere autenticación. Permite construir el registro.

Respuesta `200`:

```json
{
  "items": [
    {
      "id": 1,
      "key": "MUJER",
      "description": "Mujer"
    },
    {
      "id": 2,
      "key": "HOMBRE",
      "description": "Hombre"
    }
  ]
}
```

Los valores definitivos se cargarán como datos de catálogo en Oracle. El registro envía el `id`, no la descripción.

### `GET /catalogos/tipos-tarjeta`

Requiere autenticación.

Respuesta `200`:

```json
{
  "items": [
    {
      "id": 1,
      "key": "CREDIT",
      "description": "Crédito"
    },
    {
      "id": 2,
      "key": "DEBIT",
      "description": "Débito"
    }
  ]
}
```

### `GET /catalogos/modalidades-pago`

Requiere autenticación.

Respuesta `200`:

```json
{
  "items": [
    {
      "id": 1,
      "key": "MONTHLY",
      "description": "Mensual",
      "monthInterval": 1
    },
    {
      "id": 2,
      "key": "ANNUAL",
      "description": "Anual",
      "monthInterval": 12
    }
  ]
}
```

## 5. Autenticación y cliente

### `GET /auth/csrf`

Obtiene el token CSRF que React enviará en peticiones de escritura.

Respuesta `200`:

```json
{
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "valor-generado-por-spring-security"
}
```

### `POST /auth/registro`

No requiere sesión previa. Usa `multipart/form-data`:

- Parte `data`: JSON con tipo `application/json`.
- Parte opcional `photo`: archivo JPEG o PNG.

Contenido de `data`:

```json
{
  "name": "Emmanuel",
  "paternalSurname": "Trejo",
  "maternalSurname": "Olvera",
  "birthDate": "2000-04-01",
  "sex": "M",
  "genderId": 2,
  "fiscalIdentifier": "IDENTIFICADOR-FICTICIO",
  "email": "emmanuel@example.com",
  "password": "Contrasena-demostrativa-2026",
  "acceptsTerms": true
}
```

Validaciones iniciales:

- `name`, `paternalSurname`, `birthDate`, `sex`, `fiscalIdentifier`, `email` y `password` son obligatorios.
- `maternalSurname` y `genderId` son opcionales conforme al modelo.
- `sex` admite `M`, `F` o `X`.
- El cliente debe ser mayor de edad.
- `email` e `fiscalIdentifier` son únicos.
- La contraseña tiene como mínimo 8 caracteres.
- La confirmación de contraseña se valida exclusivamente en React y no se envía a la API.
- `acceptsTerms` debe ser `true` y no se almacena como atributo de `CLIENTE` en esta versión.
- La fotografía es opcional. Si se proporciona, debe ser JPEG o PNG, coincidir con el tipo declarado y tener un máximo de 2 MiB.

Respuesta `201`:

```json
{
  "id": 101,
  "name": "Emmanuel",
  "paternalSurname": "Trejo",
  "maternalSurname": "Olvera",
  "birthDate": "2000-04-01",
  "sex": "M",
  "gender": {
    "id": 2,
    "key": "HOMBRE",
    "description": "Hombre"
  },
  "fiscalIdentifier": "IDENTIFICADOR-FICTICIO",
  "email": "emmanuel@example.com",
  "photoUrl": "/clientes/me/foto"
}
```

El registro crea la sesión automáticamente.

`photoUrl` es `null` cuando el cliente no proporciona fotografía. No se almacena un BLOB vacío ni una imagen predeterminada.

### `POST /auth/login`

Petición:

```json
{
  "email": "emmanuel@example.com",
  "password": "Contrasena-demostrativa-2026"
}
```

Respuesta `200`:

```json
{
  "authenticated": true,
  "client": {
    "id": 101,
    "name": "Emmanuel",
    "paternalSurname": "Trejo",
    "maternalSurname": "Olvera",
    "email": "emmanuel@example.com",
    "photoUrl": "/clientes/me/foto"
  }
}
```

Credenciales inválidas responden `401` con el mismo mensaje para correo inexistente o contraseña incorrecta.

### `GET /auth/me`

Respuesta `200`:

```json
{
  "authenticated": true,
  "client": {
    "id": 101,
    "name": "Emmanuel",
    "paternalSurname": "Trejo",
    "maternalSurname": "Olvera",
    "email": "emmanuel@example.com",
    "photoUrl": "/clientes/me/foto"
  }
}
```

Sin sesión responde `401`.

### `POST /auth/logout`

Invalida la sesión y responde `204`.

### `GET /clientes/me/foto`

Devuelve el archivo de la fotografía del cliente autenticado con su tipo MIME correspondiente. Responde `404` si el recurso no está disponible.

## 5.1. Autenticación administrativa

Los usuarios internos se autentican contra `USUARIO_INTERNO` y sus permisos se obtienen de `USUARIO_INTERNO_ROL`. Las credenciales incorrectas, los usuarios inexistentes, inactivos o sin un rol administrativo admitido responden con el mismo error genérico.

### `POST /admin/auth/login`

No requiere una sesión previa, pero sí un token CSRF obtenido mediante `GET /auth/csrf`.

Petición:

```json
{
  "email": "admin.cancelaciones@virtual-insurance.test",
  "password": "contrasena-no-documentada"
}
```

Respuesta `200`:

```json
{
  "authenticated": true,
  "user": {
    "id": 1,
    "name": "Administrador academico",
    "email": "admin.cancelaciones@virtual-insurance.test",
    "roles": [
      "ADMIN",
      "CANCELLATION_REVIEWER"
    ]
  }
}
```

Credenciales inválidas responden `401 INVALID_ADMIN_CREDENTIALS` sin revelar si el correo existe o si el usuario está inactivo.

### `GET /admin/auth/me`

Requiere una sesión interna con `ADMIN` o `CANCELLATION_REVIEWER` y devuelve la misma estructura de autenticación. Una sesión de cliente recibe `403`.

### `POST /admin/auth/logout`

Requiere CSRF, invalida la sesión, elimina `JSESSIONID` y responde `204`.

La API de consulta y resolución de cancelaciones se incorporará en el siguiente incremento.

## 6. Panel del cliente

### `GET /panel`

Requiere autenticación. Resume la actividad sin obligar al frontend a combinar varias consultas.

Respuesta `200`:

```json
{
  "clientName": "Emmanuel",
  "recentActivity": [
    {
      "id": "quote-201",
      "resourceType": "QUOTE",
      "resourceId": 201,
      "typeLabel": "Cotización de auto",
      "reference": "#0002",
      "status": "APROBADA",
      "detail": "Cobertura amplia · $12,480.00 MXN anuales",
      "destination": "/cotizaciones/201"
    },
    {
      "id": "policy-301",
      "resourceType": "POLICY",
      "resourceId": 301,
      "typeLabel": "Póliza de auto",
      "reference": "VI-802145",
      "status": "ACTIVA",
      "detail": "Vigencia hasta el 25 de noviembre de 2027",
      "destination": "/polizas/301"
    }
  ]
}
```

`destination` es una ayuda para el frontend, pero la autorización siempre se vuelve a comprobar en el recurso de destino.

## 7. Cotizaciones

### Objeto de salida de cotización

Las cotizaciones de auto y médicas comparten esta respuesta:

```json
{
  "id": 201,
  "quoteNumber": "0002",
  "type": "AUTO",
  "status": "APROBADA",
  "quotedAt": "2026-11-19T14:30:00-06:00",
  "validUntil": "2026-12-19",
  "productName": "Seguro de auto",
  "coverageName": "Cobertura amplia",
  "annualCost": 12480.00,
  "monthlyCost": 1040.00,
  "currency": "MXN",
  "benefits": [
    "Daños materiales",
    "Robo total",
    "Responsabilidad civil",
    "Asistencia vial"
  ],
  "rejectionReason": null,
  "details": {
    "brand": "Mazda",
    "model": "3",
    "year": 2024,
    "category": "SEDAN",
    "plates": "ABC123A",
    "licenseNumber": "LIC-12345",
    "driverAge": 26,
    "negativePoints": 0,
    "criminalRecord": false
  }
}
```

Para una cotización rechazada:

- `status` vale `RECHAZADA`.
- `annualCost` y `monthlyCost` son `null`.
- `benefits` es una lista vacía.
- `rejectionReason` contiene el mensaje público.
- No se genera póliza ni intento de pago.

### `POST /cotizaciones/auto`

Petición:

```json
{
  "brand": "Mazda",
  "model": "3",
  "year": 2024,
  "category": "SEDAN",
  "plates": "ABC123A",
  "licenseNumber": "LIC-12345",
  "driverAge": 26,
  "negativePoints": 0,
  "criminalRecord": false
}
```

Validaciones:

- `year`: entre 1980 y el año actual más uno.
- `category`: `SEDAN`, `SUV`, `PICKUP` u `OTHER`.
- `driverAge`: entre 18 y 90.
- `negativePoints`: cero o mayor.
- Los textos obligatorios no aceptan cadenas vacías.

Regla académica inicial:

- Rechazar si `criminalRecord` es `true` o `negativePoints` es 12 o mayor.

Respuesta `201`: objeto de cotización.

### `POST /cotizaciones/medicas`

Petición:

```json
{
  "age": 35,
  "hypertension": false,
  "glucoseOutOfRange": false,
  "cholesterolOutOfRange": false,
  "smoker": false,
  "alcoholAddiction": false
}
```

Validaciones:

- `age`: entre 18 y 100.
- Los cinco factores son booleanos obligatorios.

Regla académica inicial:

- Rechazar si la edad es mayor de 70 o existen tres o más factores activos.

Respuesta `201`: objeto de cotización con `type: "MEDICAL"` y `details` médicos.

### `GET /cotizaciones/{quoteId}`

Devuelve `200` con el objeto de cotización si pertenece al cliente autenticado. En otro caso responde `404`.

### `GET /cotizaciones?limit=20`

Devuelve las cotizaciones del cliente autenticado, de la más reciente a la más antigua.

Respuesta `200`:

```json
{
  "items": [],
  "limit": 20
}
```

La primera versión no requiere paginación con cursor porque el panel solamente presenta actividad reciente.

## 8. Intentos de pago y emisión

### `POST /cotizaciones/{quoteId}/intentos-pago`

La operación registra la tarjeta ficticia, crea el intento y, si se aprueba, emite la póliza dentro de una sola transacción.

Requiere el encabezado `Idempotency-Key` con una clave aleatoria de 16 a 64 caracteres. Repetir la misma clave devuelve el intento anterior sin descontar saldo ni emitir otra póliza.

Petición:

```json
{
  "coverageStart": "2026-11-26",
  "paymentMode": "MONTHLY",
  "cardholderName": "Emmanuel Trejo Olvera",
  "cardNumber": "4242424242421111",
  "expiration": "12/30",
  "cvv": "123",
  "acceptsTerms": true
}
```

Validaciones:

- La cotización debe pertenecer al cliente, estar aprobada y vigente.
- `coverageStart` no puede estar en el pasado ni superar la vigencia de la cotización.
- `paymentMode` admite `MONTHLY` o `ANNUAL`.
- La tarjeta debe tener 16 dígitos y pertenecer a la lista académica de demostración.
- La fecha de expiración debe coincidir con el instrumento ficticio y no estar vencida.
- El CVV tiene tres dígitos, se usa para la simulación y no se almacena.
- `acceptsTerms` debe ser `true` y no se persiste.
- `cardholderName` se usa para validación de entrada, pero no se persiste porque no existe en el modelo de `TARJETA`.

Instrumentos de demostración, todos con CVV `123` y vencimiento `12/30`, salvo el escenario vencido:

| Número exclusivamente ficticio | Escenario |
|---|---|
| `4242424242421111` | Fondos suficientes; saldo inicial de $50,000 |
| `4000000000002222` | Fondos insuficientes; saldo inicial de $100 |
| `4000000000003333` | Tarjeta bloqueada |
| `4000000000004444` | Tarjeta vencida en `01/25`; se prueba por API porque React impide enviarla |
| `4000000000005555` | Rechazo forzado del procesador |
| `4242424242426666` | Saldo exacto de $1,040 para probar un débito hasta cero |

Cualquier otro número produce un intento rechazado con `TEST_CARD_NOT_FOUND`; no se emite póliza.

Respuesta aprobada `201`:

```json
{
  "attemptId": 401,
  "status": "APROBADO",
  "responseCode": "APPROVED",
  "message": "El pago fue aprobado por la pasarela academica.",
  "authorizationCode": "AUTH-0000001000",
  "lastFour": "1111",
  "amount": 1040.00,
  "currency": "MXN",
  "policyId": 301,
  "policyNumber": "VI-2026-001000",
  "coverageStart": "2026-11-26",
  "coverageEnd": "2027-11-26"
}
```

`paymentNumber` funciona como referencia de la autorización simulada; no se agrega un segundo atributo de autorización.

Respuesta rechazada `201`:

```json
{
  "attemptId": 402,
  "status": "RECHAZADO",
  "responseCode": "INSUFFICIENT_FUNDS",
  "message": "La tarjeta ficticia no tiene fondos suficientes.",
  "authorizationCode": null,
  "lastFour": "2222",
  "amount": 1040.00,
  "currency": "MXN",
  "policyId": null,
  "policyNumber": null,
  "coverageStart": "2026-11-26",
  "coverageEnd": null
}
```

El rechazo es un resultado de negocio, no una falla HTTP. Por eso también responde `201` y queda registrado.

Si la cotización ya produjo una póliza responde `409` con `QUOTE_ALREADY_PAID`.

El saldo pertenece exclusivamente a `INSTRUMENTO_PAGO_PRUEBA`, que representa al banco simulado. No se devuelve por la API. El número ficticio se cifra con AES-GCM antes de almacenarse en `TARJETA`; el CVV no se persiste.

## 9. Pólizas

### `GET /polizas`

Devuelve las pólizas del cliente autenticado.

Respuesta `200`:

```json
{
  "items": [
    {
      "id": 301,
      "policyNumber": "VI-802145",
      "status": "ACTIVA",
      "type": "AUTO",
      "productName": "Seguro de auto",
      "validFrom": "2026-11-26",
      "validUntil": "2027-11-25"
    }
  ]
}
```

### `GET /polizas/{policyId}`

Respuesta `200`:

```json
{
  "id": 301,
  "policyNumber": "VI-802145",
  "status": "ACTIVA",
  "coverageActive": true,
  "type": "AUTO",
  "productName": "Seguro de auto",
  "coverageName": "Cobertura amplia",
  "validFrom": "2026-11-26",
  "validUntil": "2027-11-25",
  "insured": {
    "label": "Vehículo asegurado",
    "value": "Mazda 3 · 2024"
  },
  "payment": {
    "mode": "MONTHLY",
    "nextPaymentDate": "2026-12-26",
    "lastPaymentAmount": 1040.00,
    "lastPaymentStatus": "APPLIED",
    "currency": "MXN"
  },
  "cancellation": null,
  "documents": [
    {
      "id": 501,
      "type": "POLICY_PDF",
      "name": "Póliza de seguro",
      "format": "PDF",
      "downloadUrl": "/polizas/301/documentos/501/contenido"
    }
  ]
}
```

Cuando existe una solicitud pendiente:

```json
{
  "status": "CANCELACION_SOLICITADA",
  "coverageActive": true,
  "cancellation": {
    "id": 601,
    "requestNumber": "CAN-0001",
    "status": "SOLICITADA",
    "requestedDate": "2026-11-21"
  }
}
```

### `GET /polizas/{policyId}/documentos/{documentId}/contenido`

Descarga un documento si póliza, documento y cliente autenticado coinciden. Una combinación inexistente o ajena responde `404 POLICY_DOCUMENT_NOT_FOUND` para no revelar recursos de otras cuentas.

Respuesta `200`:

```text
Content-Type: application/pdf
Content-Disposition: attachment; filename="poliza-VI-2026-001000.pdf"
Cache-Control: no-store
```

El cuerpo contiene un PDF generado desde los datos persistidos. La póliza incluye identificación, asegurado, vigencia, costo, cobertura y secciones contractuales académicas versionables; el comprobante incluye importe y autorización simulada. Ninguno contiene PAN, CVV, contraseña o identificador de sesión.

La representación anterior muestra únicamente los campos que cambian; la respuesta real conserva todos los campos del detalle.

Una póliza ajena o inexistente responde `404`.

## 10. Solicitudes de cancelación

### `POST /polizas/{policyId}/solicitudes-cancelacion`

Petición:

```json
{
  "reason": "FINANCIAL",
  "requestedDate": "2026-11-21",
  "comments": "Solicito revisar la cancelación.",
  "confirmed": true
}
```

Valores de `reason`:

- `NOT_NEEDED`
- `FINANCIAL`
- `SERVICE`
- `OTHER`

Validaciones:

- La póliza pertenece al cliente.
- La póliza se encuentra activa.
- No existe otra solicitud pendiente.
- `requestedDate` es obligatoriamente la fecha actual. El cliente no elige una fecha futura.
- Si la solicitud se aprueba, la cancelación tiene efecto inmediato; no existen cancelaciones programadas en esta versión.
- `comments` tiene máximo 500 caracteres.
- `confirmed` debe ser `true` y no se persiste.

Respuesta `201`:

```json
{
  "id": 601,
  "requestNumber": "CAN-0001",
  "policyId": 301,
  "status": "SOLICITADA",
  "reason": "FINANCIAL",
  "requestedDate": "2026-11-21",
  "comments": "Solicito revisar la cancelación.",
  "createdAt": "2026-11-21T16:00:00-06:00",
  "coverageActive": true,
  "policyStatus": "CANCELACION_SOLICITADA"
}
```

Una segunda solicitud pendiente responde `409` con `CANCELLATION_ALREADY_PENDING`.

La resolución administrativa de una cancelación no forma parte de la interfaz del cliente en esta versión. La tabla permite incorporarla posteriormente sin cambiar este contrato.

## 11. Transacciones obligatorias

### Registro

Una transacción crea `CLIENTE`, guarda la fotografía cuando se proporciona y establece la sesión. Ante cualquier error no debe quedar un cliente incompleto.

### Cotización

Una transacción crea:

1. `COTIZACION`.
2. Su subtipo `COTIZACION_AUTO` o `COTIZACION_MEDICA`.
3. El primer `HISTORICO_ESTATUS_COTIZACION`.

### Intento aprobado

Una transacción:

1. Crea o registra `TARJETA` con el número cifrado.
2. Crea `INTENTO_PAGO` aprobado.
3. Crea `POLIZA` activa.
4. Crea el primer `HISTORICO_ESTATUS_POLIZA`.
5. Genera los documentos iniciales.

### Intento rechazado

Una transacción:

1. Crea o registra `TARJETA` con el número cifrado.
2. Crea `INTENTO_PAGO` rechazado.
3. No crea póliza ni documentos.

### Cancelación

Una transacción:

1. Crea `SOLICITUD_CANCELACION` solicitada.
2. Cambia la póliza a `CANCELACION_SOLICITADA`.
3. Registra `HISTORICO_ESTATUS_POLIZA`.

## 12. Reglas de privacidad y serialización

Nunca se devuelven mediante la API:

- `CONTRASENA` o su hash.
- `NUMERO_TARJETA`, cifrado o descifrado.
- CVV.
- Claves criptográficas.
- Rutas internas del servidor.
- Detalles de excepciones, SQL o nombres internos de tablas.

El backend debe impedir que `toString`, trazas o serialización automática de entidades expongan estos campos. Las entidades JPA no se devolverán directamente: controladores y servicios usarán DTO explícitos.

## 13. Correspondencia entre React, API y Oracle

| React | API | Oracle |
|---|---|---|
| `brand` | `brand` | `COTIZACION_AUTO.MARCA` |
| `model` | `model` | `COTIZACION_AUTO.MODELO` |
| `year` | `year` | `COTIZACION_AUTO.ANIO` |
| `driverAge` | `driverAge` | `COTIZACION_AUTO.EDAD_CONDUCTOR` |
| `negativePoints` | `negativePoints` | `COTIZACION_AUTO.PUNTOS_NEGATIVOS` |
| `criminalRecord` | `criminalRecord` | `COTIZACION_AUTO.ANTECEDENTES_PENALES` |
| `age` | `age` | `COTIZACION_MEDICA.EDAD_ASEGURADO` |
| `hypertension` | `hypertension` | `COTIZACION_MEDICA.HIPERTENSION` |
| `glucoseOutOfRange` | `glucoseOutOfRange` | `COTIZACION_MEDICA.GLUCOSA_FUERA_RANGO` |
| `cholesterolOutOfRange` | `cholesterolOutOfRange` | `COTIZACION_MEDICA.COLESTEROL_FUERA_RANGO` |
| `smoker` | `smoker` | `COTIZACION_MEDICA.FUMADOR` |
| `alcoholAddiction` | `alcoholAddiction` | `COTIZACION_MEDICA.ADICCION_ALCOHOL` |
| `coverageStart` | `coverageStart` | `POLIZA.VIGENCIA_DESDE` |
| `paymentMode` | `paymentModeId` | `MODALIDAD_PAGO_ID` |
| `cardNumber` | `card.number` | `TARJETA.NUMERO_TARJETA`, cifrado |
| `expiration` | `expirationYear`, `expirationMonth` | `TARJETA.ANIO_EXPIRACION`, `MES_EXPIRACION` |
| `cvv` | `card.cvv` | No se persiste |
| `reason` | `reason` | `SOLICITUD_CANCELACION.MOTIVO` |
| `requestedDate` | `requestedDate` | `SOLICITUD_CANCELACION.FECHA_SOLICITADA` |
| `comments` | `comments` | `SOLICITUD_CANCELACION.COMENTARIOS` |

## 14. Alcance explícitamente excluido

- Administración de siniestros.
- Autenticación biométrica.
- Pagos reales o integración con una pasarela.
- Captura de tarjetas reales.
- Resolución administrativa mediante una interfaz separada.
- Renovación automática de pólizas.
- Recuperación y cambio de contraseña.
- Edición del perfil del cliente.
- Eliminación de tarjetas guardadas.
- Paginación avanzada.

## 15. Decisiones que debe reflejar el DDL

- Unicidad de correo, identificador fiscal, números de cotización, pago, póliza, factura y cancelación.
- Exactamente un subtipo por cotización según `TIPO_SEGURO`.
- Solo un intento aprobado utilizable para emitir una póliza por cotización.
- Relación única entre póliza e intento aprobado.
- Solo una cancelación pendiente por póliza.
- Historial consecutivo y único por cotización o póliza.
- Coherencia entre modalidad del intento y modalidad copiada a la póliza.
- Fechas y montos válidos mediante restricciones locales cuando sea posible.
- Cifrado del número de tarjeta y prohibición de persistir CVV.

