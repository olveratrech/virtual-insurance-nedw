# Registro de pruebas de Virtual Insurance

Este documento concentra las pruebas ejecutadas y confirmadas durante el desarrollo. Los README de cada componente conservan las instrucciones de ejecución; este archivo registra **qué se probó, qué tipo de prueba fue y cuál fue su último resultado conocido**.

Última actualización: **21 de noviembre de 2026**.

## Resumen

| Componente | Cobertura confirmada | Resultado actual |
|---|---:|---|
| Oracle | 7 pruebas incrementales y 1 recorrido E2E transaccional | Aprobado |
| Spring Boot | 71 pruebas automatizadas | 71 aprobadas, 0 fallidas, 0 errores, 0 omitidas |
| API REST | 9 comprobaciones HTTP manuales | Aprobado |
| React | Análisis estático, compilación, autenticación, registro, cotizaciones y pago real | Aprobado |

## Oracle Database

Los scripts se ejecutaron en `VI_APP@ETOBDA_S1`. Las pruebas que crean datos temporales finalizan con `rollback`, por lo que pueden repetirse sin conservar esos registros.

| Prueba | Tipo | Alcance principal | Resultado |
|---|---|---|---|
| `tests/01_catalogos.sql` | Estructura e integración | Tablas de catálogo, columnas, identidades, PK, restricciones únicas, checks y datos iniciales | Aprobada |
| `tests/02_clientes_tarjetas.sql` | Estructura e integración | Cliente, género y sexo, BCrypt, fotografía BLOB opcional, conservación de 70 bytes en la cuenta académica, tarjetas, tipos e índices de FK | Aprobada |
| `tests/03_cotizaciones.sql` | Integración transaccional | Secuencia de números, cotizaciones de auto y médicas, especialización exclusiva, estados e historial automático | Aprobada |
| `tests/04_pagos_polizas.sql` | Integración transaccional | Intentos de pago, aprobación única, emisión de póliza, modalidad, costo, documentos e historial | Aprobada |
| `tests/05_cancelaciones.sql` | Integración transaccional | Fechas, solicitudes duplicadas, resolución, sincronización de estado, historial y protección contra borrado | Aprobada |
| `tests/06_simulador_pagos.sql` | Estructura e integración | Pasarela académica, columnas de idempotencia, secuencias y seis instrumentos con saldos y respuestas controladas | Aprobada |
| `tests/07_administracion_cancelaciones.sql` | Estructura e integración transaccional | Usuarios internos, roles, responsable, observación, autorización, aprobación, rechazo y resolución única | Aprobada |
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
| `AdminAuthenticationIntegrationTests.logsInWithValidCredentialsAndReturnsInternalRoles` | Autenticación/integración | Login interno, sesión, datos administrativos y roles obtenidos de Oracle | Aprobada |
| `AdminAuthenticationIntegrationTests.normalizesEmailBeforeAuthentication` | Autenticación/integración | Normalización del correo administrativo antes de autenticar | Aprobada |
| `AdminAuthenticationIntegrationTests.rejectsIncorrectPasswordWithGenericResponse` | Seguridad/integración | Rechazo administrativo genérico de contraseña incorrecta | Aprobada |
| `AdminAuthenticationIntegrationTests.rejectsUnknownEmailWithSameGenericResponse` | Seguridad/integración | Respuesta indistinguible para un correo interno inexistente | Aprobada |
| `AdminAuthenticationIntegrationTests.rejectsInactiveInternalUser` | Autorización/integración | Rechazo de un usuario interno inactivo con rollback del cambio temporal | Aprobada |
| `AdminAuthenticationIntegrationTests.rejectsLoginWithoutCsrfToken` | Seguridad/integración | Protección CSRF del login administrativo | Aprobada |
| `AdminAuthenticationIntegrationTests.logsOutAndInvalidatesAdministrativeSession` | Sesión/integración | Logout administrativo, invalidación de sesión y eliminación de `JSESSIONID` | Aprobada |
| `AdminAuthenticationIntegrationTests.clientSessionCannotAccessAdministrativeEndpoints` | Autorización/integración | Una sesión con `ROLE_CLIENT` recibe `403` en rutas administrativas | Aprobada |
| `AdminAuthenticationIntegrationTests.administrativeSessionCannotAccessClientEndpoints` | Autorización/integración | Una sesión interna recibe `403` en rutas exclusivas del cliente | Aprobada |
| `QuoteIntegrationTests.createsApprovedAutoQuoteWithSubtypeAndHistory` | API/integración | Strategy de auto, costo, subtipo, historial y consulta posterior | Aprobada |
| `QuoteIntegrationTests.rejectsAutoQuoteWithTwelveNegativePoints` | Regla de negocio/integración | Rechazo de auto por 12 puntos y costo nulo | Aprobada |
| `QuoteIntegrationTests.rejectsAutoQuoteWithCriminalRecord` | Regla de negocio/integración | Rechazo de auto por antecedentes | Aprobada |
| `QuoteIntegrationTests.createsApprovedMedicalQuoteWithSubtypeAndHistory` | API/integración | Strategy médica, costo, subtipo e historial | Aprobada |
| `QuoteIntegrationTests.rejectsMedicalQuoteWithThreeActiveFactors` | Regla de negocio/integración | Rechazo médico por tres factores | Aprobada |
| `QuoteIntegrationTests.rejectsMedicalQuoteWhenAgeIsAboveSeventy` | Regla de negocio/integración | Rechazo médico por edad | Aprobada |
| `QuoteIntegrationTests.rejectsVehicleYearAfterNextCalendarYear` | Validación/integración | Rechazo de año posterior al siguiente calendario | Aprobada |
| `QuoteIntegrationTests.rejectsQuoteCreationWithoutAuthentication` | Seguridad/integración | Sesión obligatoria para crear cotizaciones | Aprobada |
| `QuoteIntegrationTests.hidesQuoteFromAnotherClient` | Autorización/integración | Ocultamiento mediante `quoteId + clientId` | Aprobada |
| `PaymentIntegrationTests.approvesPaymentDebitsBalanceAndIssuesPolicy` | E2E transaccional | Débito, intento aprobado, póliza, historial y dos documentos | Aprobada |
| `PaymentIntegrationTests.rejectsInsufficientFundsWithoutDebitingOrIssuingPolicy` | Regla transaccional | Rechazo sin débito ni póliza | Aprobada |
| `PaymentIntegrationTests.rejectsBlockedCard` | Regla de pasarela | Rechazo de instrumento bloqueado | Aprobada |
| `PaymentIntegrationTests.rejectsProcessorDecline` | Regla de pasarela | Rechazo forzado del procesador | Aprobada |
| `PaymentIntegrationTests.rejectsExpiredInstrumentEvenWhenSubmittedExpirationLooksCurrent` | Regla de pasarela | Vencimiento determinado por el instrumento del procesador | Aprobada |
| `PaymentIntegrationTests.acceptsExactBalanceAndLeavesItAtZero` | Límite transaccional | Débito con saldo exacto sin producir saldo negativo | Aprobada |
| `PaymentIntegrationTests.rejectsInvalidSecurityCodeWithoutStoringIt` | Seguridad | Rechazo de CVV y ausencia de columna persistente | Aprobada |
| `PaymentIntegrationTests.repeatsIdempotentPaymentWithoutSecondDebitOrPolicy` | Idempotencia | Misma respuesta sin segundo débito ni póliza | Aprobada |
| `PaymentIntegrationTests.rejectsPaymentWithoutAuthentication` | Seguridad | Sesión obligatoria para pagar | Aprobada |
| `PaymentIntegrationTests.validatesTermsBeforeCreatingAttempt` | Validación | Términos obligatorios antes de persistir | Aprobada |
| `PolicyIntegrationTests.returnsOwnedAutoPolicyWithPaymentAndDocumentMetadata` | API/integración | Póliza de auto, pago, autorización y dos documentos | Aprobada |
| `PolicyIntegrationTests.returnsOwnedMedicalPolicyWithClientAsInsured` | API/integración | Póliza médica y cliente como persona asegurada | Aprobada |
| `PolicyIntegrationTests.hidesPolicyFromAnotherClient` | Autorización/integración | Ocultamiento mediante `policyId + clientId` | Aprobada |
| `PolicyIntegrationTests.returnsNotFoundForUnknownPolicy` | Error/integración | `404 POLICY_NOT_FOUND` para identificador inexistente | Aprobada |
| `PolicyIntegrationTests.rejectsPolicyQueryWithoutAuthentication` | Seguridad/integración | Sesión obligatoria para consultar pólizas | Aprobada |
| `PolicyIntegrationTests.downloadsOwnedPolicyAsRealPdfWithPolicyData` | PDF/integración | PDF válido con datos de póliza, placas, modalidad localizada y plantilla contractual | Aprobada |
| `PolicyIntegrationTests.downloadsOwnedPaymentReceiptAsRealPdf` | PDF/integración | Comprobante válido con importe y autorización académica | Aprobada |
| `PolicyIntegrationTests.hidesDocumentFromAnotherClient` | Autorización/integración | Un cliente no puede descargar documentos ajenos | Aprobada |
| `PolicyIntegrationTests.returnsNotFoundForDocumentOutsidePolicy` | Autorización/integración | Se valida conjuntamente póliza y documento | Aprobada |
| `PolicyIntegrationTests.rejectsDocumentDownloadWithoutAuthentication` | Seguridad/integración | Sesión obligatoria para descargar | Aprobada |
| `PolicyIntegrationTests.downloadsPolicyWithLocalizedPendingCancellationAndRequestNumber` | PDF/integración | Estado de cancelación legible, folio real y ausencia de la clave técnica en el PDF | Aprobada |
| `CancellationIntegrationTests.createsCancellationAndUpdatesPolicyAndHistory` | API/integración | Creación persistente, respuesta, cambio de póliza e historial automático | Aprobada |
| `CancellationIntegrationTests.rejectsDuplicatePendingCancellation` | Regla transaccional | Rechazo `409 CANCELLATION_ALREADY_PENDING` de una segunda solicitud pendiente | Aprobada |
| `CancellationIntegrationTests.hidesPolicyOwnedByAnotherClient` | Autorización/integración | Ocultamiento de una póliza ajena mediante `404 POLICY_NOT_FOUND` | Aprobada |
| `CancellationIntegrationTests.returnsNotFoundForUnknownPolicy` | Error/integración | `404 POLICY_NOT_FOUND` para un identificador inexistente | Aprobada |
| `CancellationIntegrationTests.rejectsPolicyThatIsNotActive` | Estado/integración | Rechazo `409 POLICY_NOT_ACTIVE` cuando la póliza no está activa | Aprobada |
| `CancellationIntegrationTests.validatesPastDateInvalidReasonLongCommentsAndConfirmation` | Validación/integración | Fecha, motivo, longitud de comentarios y confirmación obligatoria | Aprobada |
| `CancellationIntegrationTests.rejectsFutureRequestedDate` | Validación/integración | Rechazo de una fecha futura aunque se intente omitir la restricción de React | Aprobada |
| `CancellationIntegrationTests.rejectsRequestWithoutAuthentication` | Seguridad/integración | Sesión obligatoria para solicitar cancelación | Aprobada |
| `CancellationIntegrationTests.rejectsRequestWithoutCsrf` | Seguridad/integración | Protección CSRF de la operación de escritura | Aprobada |
| `PanelIntegrationTests.returnsOwnedQuoteWithContractFieldsAndDestination` | API/integración | Cotización propia, contrato de actividad y destino navegable | Aprobada |
| `PanelIntegrationTests.ordersNewPolicyBeforeItsQuote` | Orden/integración | Póliza recién emitida antes de la cotización que la originó | Aprobada |
| `PanelIntegrationTests.reflectsPendingCancellationInPolicyActivity` | Estado/integración | Estado actualizado de una póliza con cancelación pendiente | Aprobada |
| `PanelIntegrationTests.returnsEmptyActivityForClientWithoutOperations` | Caso vacío/integración | Nombre del cliente y lista vacía sin operaciones | Aprobada |
| `PanelIntegrationTests.excludesActivityOwnedByAnotherClient` | Autorización/integración | Exclusión de actividad perteneciente a otro cliente | Aprobada |
| `PanelIntegrationTests.rejectsPanelWithoutAuthentication` | Seguridad/integración | Sesión obligatoria para consultar el panel | Aprobada |

Última ejecución confirmada: **71 pruebas, 0 fallidas, 0 errores y 0 omitidas; `BUILD SUCCESS`**.

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
| `POST /cotizaciones/auto` con sesión y CSRF | E2E parcial HTTP | `201`, auto aprobado, costos y datos normalizados |
| `POST /cotizaciones/medicas` con sesión y CSRF | E2E parcial HTTP | `201`, médica rechazada, costos nulos y motivo público |
| `GET /cotizaciones/{quoteId}` con propietario | Integración HTTP | `200`, devuelve los datos persistidos |
| `GET /cotizaciones/{quoteId}` sin sesión | Seguridad HTTP | `401` |

No se registran contraseñas, tokens CSRF, identificadores de sesión ni otros secretos como evidencia.

## React

El recorrido comercial principal del frontend consume la API real. Las pruebas manuales complementan las pruebas automatizadas de Spring y verifican navegación, presentación y comportamiento en el navegador.

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
| Pólizas de auto y médicas, pagos y documentos PDF reales | Manual funcional y visual | Aprobado |
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
| Carga del catálogo de géneros en el registro | Integración manual React–Spring–Oracle | Aprobada; muestra los cinco registros y permite omitir género |
| Validaciones del registro ampliado | Manual funcional | Aprobadas; obligatoriedad, edad, fecha mínima, contraseñas, términos y archivo |
| Registro sin fotografía ni género | E2E parcial navegador–Oracle | Aprobado; crea cliente con valores `NULL`, sesión y navegación al panel |
| Persistencia de la cuenta registrada | Verificación Oracle | Aprobada; datos normalizados y cliente único |
| Sesión automática después del registro | Integración manual | Aprobada; persiste tras recarga y permite login posterior |
| Correo o identificador duplicado | Regresión React–Spring | Aprobado; muestra el error, no duplica y limpia contraseñas |
| Formulario ampliado en diseño adaptable | Revisión visual | Aprobado en escritorio y vista móvil |
| Retorno a `/cotizar/auto` después de autenticarse | Regresión de navegación protegida | Aprobado; conserva el destino solicitado antes del login |
| Cotización de auto aprobada y rechazada | E2E parcial React–Spring–Oracle | Aprobado; persiste la decisión y permite recargar el resultado |
| Cotización médica aprobada y rechazada | E2E parcial React–Spring–Oracle | Aprobado; persiste la decisión y permite recargar el resultado |
| Revisión de datos desde resultados rechazados | Regresión de navegación | Aprobado; restaura los datos de ambos productos |
| Prevención de doble envío durante la evaluación | Regresión manual | Aprobado |
| Continuación a pago y regreso al resultado | Integración manual | Aprobado; conserva la cotización seleccionada |
| Cotizaciones integradas en diseño adaptable | Revisión visual | Aprobada |
| Consulta de una cotización inexistente | Integración de error React–Spring | Aprobada; muestra la página React, el detalle del `404` y acciones de recuperación |
| Pago con fondos suficientes y emisión de póliza | E2E parcial React–Spring–Oracle | Aprobado; descuenta saldo, registra el intento y muestra la póliza activa |
| Pago con fondos insuficientes | Regla transaccional manual | Aprobado; conserva la pantalla, no descuenta ni emite póliza |
| Tarjeta bloqueada y rechazo del procesador | Regla de pasarela manual | Aprobados; muestran motivos controlados y permiten reintentar |
| CVV incorrecto e instrumento no registrado | Seguridad y error manual | Aprobados; no exponen datos ni emiten póliza |
| Pago con saldo exacto | Prueba de límite E2E | Aprobado; emite póliza y deja saldo en cero |
| Doble envío del formulario de pago | Regresión de idempotencia | Aprobado; bloquea el botón y genera una sola póliza |
| Validaciones locales de pago | Regresión manual | Aprobadas; fecha, PAN, vencimiento, CVV y términos |
| Póliza recién emitida con datos persistidos | E2E parcial React–Spring–Oracle | Aprobada; muestra folio, estado, asegurado, pago y documentos |
| Recarga y acceso directo a póliza | Persistencia y navegación | Aprobados; reconstruyen la pantalla mediante la API |
| Consulta persistente de póliza médica | E2E parcial React–Spring–Oracle | Aprobada; muestra producto, cobertura y persona asegurada |
| Póliza inexistente | Integración de error React–Spring | Aprobada; presenta el `404` dentro de React y permite reintentar |
| Retorno a póliza después de autenticarse | Regresión de navegación protegida | Aprobado; conserva la URL solicitada |
| Metadatos de documentos persistidos | Integración manual | Aprobados; ambos documentos provienen de Oracle y exponen su descarga protegida |
| Descarga de póliza y comprobante PDF | E2E parcial React–Spring–Oracle | Aprobada; ambos archivos abren correctamente y contienen datos persistidos |
| Contenido PDF de póliza de auto | Regresión funcional y visual | Aprobado; incluye placas y muestra la modalidad como Mensual o Anual |
| Solicitud real de cancelación | E2E parcial React–Spring–Oracle | Aprobada; crea el folio y persiste motivo, fecha, comentarios y estado |
| Recarga de póliza con cancelación pendiente | Persistencia y navegación | Aprobada; conserva `CANCELACION_SOLICITADA`, cobertura activa y oculta la acción duplicada |
| Acceso directo a cancelación pendiente | Regresión de duplicados | Aprobado; informa que la solicitud ya está en revisión sin mostrar el formulario |
| PDF de póliza activa | Regresión funcional y visual | Aprobado; muestra `Activa` y no presenta folio de cancelación |
| PDF de póliza con solicitud pendiente | Regresión funcional y visual | Aprobado; muestra `Cancelacion solicitada` y el folio real de la solicitud |
| Panel con actividad persistida | E2E parcial React–Spring–Oracle | Aprobado; muestra cotizaciones y pólizas propias ordenadas por actividad reciente |
| Navegación desde actividad del panel | Integración manual | Aprobada; cada elemento abre su cotización o póliza real |
| Póliza con cancelación en el panel | Regresión de estado | Aprobada; aparece reciente con `CANCELACIÓN SOLICITADA` y tono de advertencia |
| Panel sin operaciones | Caso vacío manual | Aprobado; muestra el mensaje de actividad vacía sin datos ficticios |
| Panel real en diseño adaptable | Revisión visual | Aprobado en escritorio y vista estrecha |
| Fecha efectiva fija de cancelación | Integración manual React–Spring–Oracle | Aprobada; React muestra el día actual sin permitir cambios y Oracle conserva esa misma fecha |

## Incidencias detectadas y resueltas durante las pruebas

- Oracle devolvió `ORA-01031` al crear columnas `identity`; se agregó el privilegio mínimo `create sequence` a `VI_APP` y se repitió exitosamente el incremento.
- Oracle devolvió `ORA-25006` porque un BLOB aparecía en `update of`; se corrigió el trigger de intentos de pago y las pruebas posteriores aprobaron.
- La primera prueba del catálogo asumía un orden semántico distinto al orden real de las identidades. Se corrigió para verificar los registros y el orden por ID sin imponer significado comercial a la PK.
- La validación inicial del login rechazaba con `422` un correo con espacios exteriores antes de normalizarlo. La limpieza se trasladó al constructor del DTO y las 16 pruebas aprobaron.
- El proxy inicial de Vite no reenviaba `/cotizaciones` y producía un `404` al crear una cotización. Al agregarlo, también interceptaba la recarga de la ruta SPA `/cotizaciones/:id`; se separaron las llamadas técnicas bajo `/api` con reescritura hacia Spring Boot.
- La prueba de saldo exacto dependía del saldo inicial del seed y falló después de una demostración manual. Se aisló preparando $1,040 dentro de la propia transacción; el rollback restaura el estado anterior.
- La primera ejecución de la suite administrativa produjo 71 errores porque había dos beans `AuthenticationManager` sin uno principal para construir `HttpSecurity`. Se marcó el gestor de clientes como `@Primary`; ambos servicios conservan calificadores explícitos y la repetición terminó 71/71.

## Pendientes de prueba

- Descarga protegida de la fotografía del cliente.
- Recorrido E2E completo desde el navegador hasta Oracle.
- Pruebas automatizadas del frontend y del recorrido entre frontend y backend.

## Regla de mantenimiento

Después de cada incremento se debe agregar o actualizar aquí el nombre de la prueba, su tipo, el comportamiento cubierto y el último resultado confirmado. Una prueba manual solo se marca como aprobada después de que el autor del proyecto confirma su ejecución.
