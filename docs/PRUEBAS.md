# Registro de pruebas de Virtual Insurance

Este documento concentra las pruebas ejecutadas y confirmadas durante el desarrollo. Los README de cada componente conservan las instrucciones de ejecución; este archivo registra **qué se probó, qué tipo de prueba fue y cuál fue su último resultado conocido**.

Última actualización: **20 de noviembre de 2026**.

## Resumen

| Componente | Cobertura confirmada | Resultado actual |
|---|---:|---|
| Oracle | 5 pruebas incrementales y 1 recorrido E2E transaccional | Aprobado |
| Spring Boot | 17 pruebas automatizadas | 17 aprobadas, 0 fallidas, 0 errores, 0 omitidas |
| API REST | 9 comprobaciones HTTP manuales | Aprobado |
| React | Análisis estático, compilación, autenticación real y recorridos funcionales | Aprobado; autenticación integrada y módulos comerciales todavía simulados |

## Oracle Database

Los scripts se ejecutaron en `VI_APP@ETOBDA_S1`. Las pruebas que crean datos temporales finalizan con `rollback`, por lo que pueden repetirse sin conservar esos registros.

| Prueba | Tipo | Alcance principal | Resultado |
|---|---|---|---|
| `tests/01_catalogos.sql` | Estructura e integración | Tablas de catálogo, columnas, identidades, PK, restricciones únicas, checks y datos iniciales | Aprobada |
| `tests/02_clientes_tarjetas.sql` | Estructura e integración | Cliente, género y sexo, BCrypt, fotografía BLOB opcional, conservación de 70 bytes en la cuenta académica, tarjetas, tipos e índices de FK | Aprobada |
| `tests/03_cotizaciones.sql` | Integración transaccional | Cotizaciones de auto y médicas, especialización exclusiva, estados e historial automático | Aprobada |
| `tests/04_pagos_polizas.sql` | Integración transaccional | Intentos de pago, aprobación única, emisión de póliza, modalidad, costo, documentos e historial | Aprobada |
| `tests/05_cancelaciones.sql` | Integración transaccional | Fechas, solicitudes duplicadas, resolución, sincronización de estado, historial y protección contra borrado | Aprobada |
| `tests/99_recorrido_completo.sql` | E2E transaccional | Recorrido completo del caso de estudio y reglas negativas, con rollback integral | Aprobada |

Los scripts de estructura y carga también se ejecutaron dos veces durante cada incremento para verificar su idempotencia o repetibilidad sin duplicar datos.

### Caminos del recorrido E2E

| Camino | Resultado esperado y obtenido |
|---|---|
| Cotización de auto rechazada | Se impide iniciar el pago (`-20060`) |
| Cotización médica rechazada | Se impide iniciar el pago (`-20060`) |
| Auto aprobado con pago rechazado | No se emite póliza; se bloquea la emisión desde el intento rechazado (`-20065`) |
| Médico aprobado con pago rechazado | No se emite póliza |
| Auto aprobado, pagado y emitido | Se crean póliza y documentos; se rechazan pago aprobado duplicado (`-20064`), fecha de cancelación pasada (`-20083`) y solicitud duplicada (`-20082`); una cancelación rechazada conserva la póliza activa |
| Médico aprobado, pagado y emitido | Una cancelación aprobada deja la póliza cancelada; se bloquean otra solicitud (`-20082`) y la eliminación de la solicitud (`-20080`) |

La ejecución integrada produjo temporalmente 1 cliente, 6 cotizaciones, 4 intentos de pago, 2 pólizas, 4 documentos y 2 solicitudes de cancelación. Las cinco verificaciones posteriores al `rollback` devolvieron cero.

## Spring Boot: pruebas automatizadas

Estas pruebas se ejecutan con `./mvnw test` y utilizan la instancia real de Oracle configurada mediante variables de entorno. Las pruebas de registro son transaccionales y revierten sus cambios al finalizar.

| Nombre de la prueba | Tipo | Verifica | Resultado |
|---|---|---|---|
| `VirtualInsuranceApiApplicationTests.contextLoads` | Contexto/integración | Arranque del contexto de Spring y configuración del datasource | Aprobada |
| `GenderRepositoryIntegrationTests.findsAllSeededGendersOrderedById` | Repositorio/integración | Lectura de los cinco géneros reales y orden ascendente por ID | Aprobada |
| `RegistrationIntegrationTests.registersClientHashesPasswordStoresPhotoAndCreatesSession` | API/integración | Alta del cliente, hash BCrypt, BLOB de fotografía y creación de sesión | Aprobada |
| `RegistrationIntegrationTests.registersClientWithoutOptionalPhoto` | API/integración | Alta multipart sin fotografía, `FOTO = NULL`, `photoUrl = null` y sesión | Aprobada |
| `RegistrationIntegrationTests.rejectsDuplicateEmail` | Regla de negocio/integración | Rechazo de correo duplicado | Aprobada |
| `RegistrationIntegrationTests.rejectsUnderageClient` | Validación/integración | Rechazo de cliente menor de edad | Aprobada |
| `RegistrationIntegrationTests.rejectsContentThatDoesNotMatchDeclaredPhotoType` | Seguridad/integración | Validación del contenido real de la fotografía frente a su tipo declarado | Aprobada |
| `RegistrationIntegrationTests.rejectsTermsNotAccepted` | Validación/integración | Rechazo cuando no se aceptan los términos | Aprobada |
| `RegistrationIntegrationTests.rejectsUnknownGender` | Validación/integración | Rechazo de un género inexistente | Aprobada |
| `RegistrationIntegrationTests.rejectsRegistrationWithoutCsrfToken` | Seguridad/integración | Protección CSRF del registro | Aprobada |
| `AuthenticationIntegrationTests.logsInWithValidCredentialsAndCreatesSession` | Autenticación/integración | Login BCrypt correcto, respuesta y sesión autenticada | Aprobada |
| `AuthenticationIntegrationTests.rejectsIncorrectPasswordWithGenericResponse` | Seguridad/integración | Rechazo genérico de contraseña incorrecta | Aprobada |
| `AuthenticationIntegrationTests.rejectsUnknownEmailWithSameGenericResponse` | Seguridad/integración | Mismo rechazo genérico para un correo inexistente | Aprobada |
| `AuthenticationIntegrationTests.rejectsLoginWithoutCsrfToken` | Seguridad/integración | Protección CSRF del login | Aprobada |
| `AuthenticationIntegrationTests.normalizesEmailBeforeAuthentication` | Autenticación/integración | Eliminación de espacios y normalización de mayúsculas del correo | Aprobada |
| `AuthenticationIntegrationTests.logsOutAndInvalidatesSession` | Sesión/integración | Logout, invalidación de sesión y eliminación de `JSESSIONID` | Aprobada |
| `AuthenticationIntegrationTests.rejectsSessionCheckAfterLogout` | Seguridad/integración | Rechazo de consulta de sesión después del logout | Aprobada |

Última ejecución confirmada: **17 pruebas, 0 fallidas, 0 errores y 0 omitidas; `BUILD SUCCESS`**.

## API REST: comprobaciones manuales

| Comprobación | Tipo | Resultado |
|---|---|---|
| Inicio de Spring Boot con Oracle | Integración manual | Correcto |
| `GET /actuator/health` | Smoke test HTTP | `200`, estado `UP` |
| `GET /catalogos/generos` | Integración HTTP | `200`, cinco registros provenientes de Oracle |
| `POST /catalogos/generos` | Seguridad HTTP | `403` |
| `POST /auth/registro` con multipart, CSRF y datos válidos | E2E parcial HTTP | `201`, cliente persistido y sesión creada |
| `GET /auth/me` | Sesión HTTP | `200` con cookie; `401` sin cookie |
| `POST /auth/login` con cuenta académica y CSRF | Autenticación HTTP | `200`, datos básicos y sesión autenticada |
| `POST /auth/logout` con sesión y CSRF | Seguridad HTTP | `204`, cuerpo vacío y expiración de `JSESSIONID` |
| `GET /auth/me` después del logout | Regresión de sesión HTTP | `401`; la sesión anterior dejó de autenticar |

No se registran contraseñas, tokens CSRF, identificadores de sesión ni otros secretos como evidencia.

## React

El frontend continúa utilizando datos simulados en los módulos que todavía no consumen la API. Por ello, las siguientes son pruebas del comportamiento de interfaz y no sustituyen las futuras pruebas de integración React–Spring.

| Prueba o recorrido | Tipo | Resultado |
|---|---|---|
| `npm run lint` | Análisis estático | Aprobado en los incrementos implementados |
| `npm run build` | Compilación de producción | Aprobado en los incrementos implementados |
| Inicio, acceso y registro | Manual funcional y visual | Aprobado |
| Panel, navegación y actividad reciente | Manual funcional y visual | Aprobado |
| Cotización de auto y médica, con aprobaciones y rechazos | Manual funcional y visual | Aprobado |
| Conservación de datos al revisar una cotización rechazada | Regresión manual | Aprobado |
| Contratación y pagos simulados aprobados y rechazados | Manual funcional | Aprobado |
| Conservación del producto al regresar desde pago | Regresión manual | Aprobado |
| Pólizas de auto y médicas, pagos y documentos simulados | Manual funcional y visual | Aprobado |
| Solicitud de cancelación y bloqueo de duplicados | Manual funcional | Aprobado |
| Rechazo de fechas pasadas y tarjetas ficticias vencidas | Regresión manual | Aprobado |
| Rutas y pólizas inexistentes | Regresión manual | Aprobado; muestran 404 explícito |
| Adaptación visual de las pantallas implementadas | Revisión visual | Aprobada |
| Acceso directo a `/panel` sin sesión | Integración manual React–Spring | Aprobado; redirige a `/acceso` |
| Login con contraseña incorrecta | Integración manual React–Spring | Aprobado; conserva el acceso bloqueado y muestra el error |
| Login con cuenta académica | E2E parcial navegador–Oracle | Aprobado; abre el panel y muestra los datos del cliente |
| Recarga del panel autenticado | Regresión de sesión | Aprobada; reconstruye la sesión mediante `/auth/me` |
| Acceso a `/acceso` con sesión | Navegación autenticada | Aprobado; regresa al panel |
| Logout desde el encabezado | Integración manual React–Spring | Aprobado; vuelve al inicio y actualiza el encabezado |
| Acceso a `/panel` después del logout | Regresión de autorización | Aprobado; redirige nuevamente a `/acceso` |
| Integración de autenticación en diseño adaptable | Revisión visual | Aprobada |

## Incidencias detectadas y resueltas durante las pruebas

- Oracle devolvió `ORA-01031` al crear columnas `identity`; se agregó el privilegio mínimo `create sequence` a `VI_APP` y se repitió exitosamente el incremento.
- Oracle devolvió `ORA-25006` porque un BLOB aparecía en `update of`; se corrigió el trigger de intentos de pago y las pruebas posteriores aprobaron.
- La primera prueba del catálogo asumía un orden semántico distinto al orden real de las identidades. Se corrigió para verificar los registros y el orden por ID sin imponer significado comercial a la PK.
- La validación inicial del login rechazaba con `422` un correo con espacios exteriores antes de normalizarlo. La limpieza se trasladó al constructor del DTO y las 16 pruebas aprobaron.

## Pendientes de prueba

- Descarga protegida de la fotografía del cliente.
- Integración de React con la API real, sustituyendo los mocks.
- Autorización por propietario sobre cotizaciones, pagos, pólizas, documentos y cancelaciones.
- Recorrido E2E completo desde el navegador hasta Oracle.
- Pruebas automatizadas del frontend y del recorrido entre frontend y backend.

## Regla de mantenimiento

Después de cada incremento se debe agregar o actualizar aquí el nombre de la prueba, su tipo, el comportamiento cubierto y el último resultado confirmado. Una prueba manual solo se marca como aprobada después de que el autor del proyecto confirma su ejecución.
