# Patrones de diseño y arquitectura

Este documento registra los patrones utilizados deliberadamente en Virtual Insurance, el problema que resuelven y los archivos donde pueden verificarse. No se busca acumular patrones: cada uno debe reducir acoplamiento, concentrar una responsabilidad o facilitar pruebas y mantenimiento.

Última actualización: **23 de noviembre de 2026**.

## Resumen

| Patrón o enfoque | Capa | Estado | Propósito principal |
|---|---|---|---|
| Arquitectura por capas | Backend | Implementado | Separar HTTP, negocio, persistencia y base de datos |
| MVC | Backend | Implementado mediante Spring MVC | Separar controladores, modelos de entrada/salida y servicios |
| Repository | Backend | Implementado | Abstraer persistencia y consultas JPA |
| Service Layer | Backend | Implementado | Concentrar reglas, coordinación y transacciones |
| DTO | Backend | Implementado | Definir contratos sin exponer entidades JPA |
| Adapter | Backend | Implementado | Adaptar clientes y usuarios internos a Spring Security sin acoplar las entidades |
| Dependency Injection | Backend | Implementado mediante Spring | Desacoplar dependencias y facilitar pruebas |
| Component Composition | Frontend | Implementado | Construir pantallas mediante componentes reutilizables |
| Provider / Context | Frontend | Implementado | Compartir por separado las sesiones de cliente y administración sin prop drilling |
| Facade de servicios | Frontend | Implementado | Ocultar `fetch`, cookies, CSRF, multipart y rutas HTTP |
| Route Guard | Frontend | Implementado | Impedir acceso visual a rutas que requieren sesión |
| Responsive Application Shell | Frontend | Implementado | Mantener una navegación coherente entre sidebar de escritorio y drawer móvil |
| Strategy | Backend | Implementado | Separar reglas de cotización de auto y médica |
| Ports and Adapters | Backend | Implementado | Aislar el caso de uso de pago del procesador controlado |
| Template | Backend | Implementado | Separar textos contractuales editables de la generación PDF |
| State simplificado | Backend y Oracle | Implementado | Controlar transiciones válidas de estados comerciales |

## Flujo arquitectónico actual

```text
React
  -> Context y componentes
  -> servicios de API
  -> Controller de Spring MVC
  -> Service Layer
  -> Repository de Spring Data JPA
  -> Oracle Database 26ai
```

La autenticación agrega un adaptador hacia Spring Security:

```text
AuthenticationService
  -> clientAuthenticationManager
  -> ClientUserDetailsService (Adapter)
  -> ClientRepository
  -> CLIENTE

AdminAuthenticationService
  -> adminAuthenticationManager
  -> InternalUserDetailsService (Adapter)
  -> InternalUserRepository
  -> USUARIO_INTERNO + roles
```

## Patrones implementados en el backend

### Arquitectura por capas

**Problema:** mezclar recepción HTTP, reglas de negocio y SQL produciría clases difíciles de modificar y probar.

**Aplicación:**

- Los controladores reciben y responden HTTP.
- Los servicios coordinan reglas y transacciones.
- Los repositorios acceden a la persistencia.
- Las entidades representan datos almacenados en Oracle.

**Ejemplos:**

- `backend/src/main/java/mx/unam/virtualinsurance/auth/AuthenticationController.java`
- `backend/src/main/java/mx/unam/virtualinsurance/auth/AuthenticationService.java`
- `backend/src/main/java/mx/unam/virtualinsurance/customer/ClientRepository.java`
- `backend/src/main/java/mx/unam/virtualinsurance/customer/Client.java`

### MVC con Spring

**Problema:** la API necesita separar el protocolo HTTP de las operaciones del dominio.

**Aplicación:** los controladores de Spring MVC reciben solicitudes, validan DTO y delegan el trabajo. La lógica de registro o autenticación no se implementa dentro del controlador.

**Ejemplos:** `AuthenticationController`, `GenderController` y `CsrfController`.

### Repository

**Problema:** los servicios no deben depender de consultas SQL o del ciclo de vida de JPA.

**Aplicación:** `ClientRepository` y `GenderRepository` exponen operaciones de persistencia con nombres del dominio, como búsqueda por correo o clave de género.

**Beneficio:** las pruebas ejercitan la integración real con Oracle sin colocar consultas en controladores o servicios.

### Service Layer

**Problema:** una operación como registro involucra validaciones, normalización, BCrypt, fotografía, persistencia y creación de sesión.

**Aplicación:**

- `RegistrationService` coordina el alta transaccional.
- `AuthenticationService` coordina login y consulta de la sesión.
- `SecuritySessionService` encapsula la creación y persistencia del contexto de seguridad.
- `GenderService` transforma el catálogo para la API.

**Beneficio:** cada caso de uso tiene un punto de entrada claro y los controladores permanecen pequeños.

### DTO

**Problema:** devolver entidades JPA expondría atributos internos, relaciones perezosas y datos sensibles como el hash de contraseña o el BLOB de fotografía.

**Aplicación:** records como `RegistrationRequest`, `RegistrationResponse`, `LoginRequest`, `AuthenticationStatusResponse` y `GenderResponse` definen entradas y salidas explícitas.

**Regla:** ninguna entidad JPA se devuelve directamente desde un controlador.

### Adapter

**Problema:** Spring Security autentica mediante `UserDetailsService`, pero el modelo propio tiene dos identidades distintas: clientes comerciales y usuarios internos.

**Aplicación:** `ClientUserDetailsService` traduce un cliente a `ClientPrincipal`; `InternalUserDetailsService` traduce un usuario interno y sus roles a `InternalUserPrincipal`. Cada adaptador utiliza un `AuthenticationManager` calificado y separado.

**Beneficio:** las entidades no dependen de Spring Security, un correo coincidente no mezcla fuentes de identidad y las rutas pueden exigir `ROLE_CLIENT`, `ROLE_ADMIN` o `ROLE_CANCELLATION_REVIEWER` explícitamente.

### Dependency Injection

**Problema:** crear repositorios, codificadores o servicios manualmente dentro de cada clase generaría acoplamiento rígido.

**Aplicación:** Spring inyecta dependencias mediante constructores. `SecurityConfig` declara las piezas configurables, como `PasswordEncoder`, `AuthenticationManager` y `SecurityContextRepository`.

**Nota:** los beans tienen alcance singleton por defecto, pero no se presenta como una implementación manual del patrón Singleton.

## Patrones implementados en el frontend

### Component Composition

**Problema:** repetir campos, tarjetas, estados y estructuras de página haría inconsistente la interfaz.

**Aplicación:** páginas como `AccessPage` y `DashboardPage` se construyen con componentes enfocados, entre ellos `LoginForm`, `RegisterForm`, `FormField`, `SelectField`, `CheckboxField` y `DashboardActionCard`.

**Beneficio:** una corrección de accesibilidad o estilo puede aplicarse en un componente compartido.

### Provider / Context

**Problema:** encabezado, formulario de acceso, panel y rutas protegidas necesitan conocer la misma sesión.

**Aplicación:** `AuthProvider` conserva el cliente y su sesión; `AdminAuthProvider` conserva al usuario interno y sus roles. `useAuth` y `useAdminAuth` impiden que los componentes mezclen ambos contextos.

**Archivos:**

- `frontend/src/contexts/AuthContext.jsx`
- `frontend/src/contexts/authContext.js`
- `frontend/src/hooks/useAuth.js`
- `frontend/src/contexts/AdminAuthContext.jsx`
- `frontend/src/contexts/adminAuthContext.js`
- `frontend/src/hooks/useAdminAuth.js`

**Beneficio:** se evita pasar datos de autenticación manualmente y cada árbol de rutas monta solamente el proveedor que le corresponde.

### Facade de servicios HTTP

**Problema:** los componentes no deben repetir `fetch`, cookies, CSRF, serialización multipart ni interpretación de `ProblemDetail`.

**Aplicación:**

- `apiClient.js` maneja transporte y errores comunes.
- `authApi.js` expone operaciones de autenticación y oculta CSRF y multipart.
- `catalogApi.js` expone la lectura de catálogos.

**Beneficio:** los componentes llaman operaciones como `login`, `register` o `getGenders` sin conocer detalles del protocolo.

El mismo criterio se aplica al área de cuenta: `AccountHistoryService` obtiene una proyección ordenada del repositorio y la separa por tipo de movimiento. `PanelService` reutiliza la misma transformación, pero limita el resultado a tres elementos. React consume ambos contratos mediante `panelApi.js` y reutiliza `AccountHistoryPage` para cotizaciones, pólizas y pagos, evitando tres implementaciones visuales equivalentes.

`ProfileService` concentra normalización, campos editables, verificación BCrypt para el cambio de correo, unicidad y tratamiento de fotografía. `ProfileController` solo traduce HTTP y obtiene la identidad desde la sesión; `ProfilePage` replica validaciones de interacción, pero Spring conserva la autoridad sobre propiedad, credenciales y archivos.

### Route Guard

**Problema:** una persona sin sesión no debe visualizar el panel del cliente.

**Aplicación:** `ProtectedRoute` protege el portal comercial y `AdminProtectedRoute` protege `/admin`; ambos consultan su contexto, manejan carga y errores, y redirigen a su acceso correspondiente.

**Límite:** esta protección mejora la navegación, pero no sustituye la autorización del backend. Spring debe validar siempre al propietario del recurso.

### Responsive Application Shell

**Problema:** el encabezado horizontal anterior no escalaba con todos los módulos ni ofrecía una navegación móvil controlada.

**Aplicación:** `PortalShell` compone una sidebar fija para escritorio y una topbar con drawer para anchos menores a 1024 px. `PortalNavigation` reutiliza la misma definición de rutas en ambos contenedores. El drawer cierra al navegar, pulsar el fondo o presionar Escape, mientras `MainLayout` decide entre shell público y autenticado mediante `AuthContext`.

**Archivos:**

- `frontend/src/components/layout/PortalShell/PortalShell.jsx`
- `frontend/src/components/layout/PortalShell/PortalShell.css`
- `frontend/src/layouts/MainLayout.jsx`

**Beneficio:** una sola fuente de navegación evita que escritorio y móvil diverjan y permite incorporar módulos sin volver al desplazamiento horizontal.

Las reglas de entrada se concentran en `inputHygiene.js` y se componen con `FormField` y `SelectField`. Se documentan como utilidades compartidas, no como un patrón adicional: limpian escritura y pegado, formatean tarjeta y vencimiento y enfocan el primer error, mientras el backend conserva la validación autoritativa.

## Strategy para cotizaciones

Auto y médico comparten el caso de uso de cotización, pero tienen datos y reglas diferentes. Se implementó una estrategia por producto detrás de un servicio coordinador común.

`QuoteStrategy<T>` define la evaluación común. `AutoQuoteStrategy` y `MedicalQuoteStrategy` producen una `QuoteDecision` sin depender de HTTP, sesión o persistencia; `QuoteService` coordina la transacción y el subtipo. Las pruebas cubren decisiones aprobadas y rechazadas de ambas estrategias.

No se crean estrategias adicionales para reglas idénticas.

## Puerto y adaptador de pagos

`PaymentGateway` es el puerto requerido por el caso de uso y `SimulatedPaymentGateway` es el adaptador que consulta el procesador controlado en Oracle. `PaymentService` depende del contrato, no de la implementación concreta, por lo que una pasarela externa podría sustituirlo sin mover sus detalles al controlador ni al frontend.

La abstracción no oculta la transacción: el servicio coordina idempotencia, validación de la red mediante `CardBrand`, intento, débito, póliza, historial y documentos. `CardBrand` concentra prefijos, longitud y tamaño del código de seguridad para Visa, Mastercard y American Express; React replica estas reglas solo para respuesta inmediata y Spring conserva la autoridad. Las respuestas del procesador se representan con `GatewayPaymentResult` y nunca exponen el número completo ni el CVV.

`QuoteService` proyecta la relación persistida con la póliza mediante `contracted`, `policyId` y `policyStatus`. React usa esa proyección para navegación y presentación, mientras `PaymentService` vuelve a consultar la existencia de la póliza dentro de la operación de pago y responde `QUOTE_ALREADY_PAID`; el estado de cancelación no habilita nuevamente la cotización.

## Plantilla de documentos

`PolicyDocumentGenerator` define la composición visual común de los PDFs, carga el logotipo y las familias Cinzel, Fira Sans y JetBrains Mono desde los recursos del backend, y las incrusta en cada archivo. La póliza aplica una jerarquía tipográfica y cromática según títulos, texto, datos técnicos y estados. `AcademicContractContent` concentra las seis secciones contractuales editables, su variante de cobertura por producto y la versión. `PolicyPdfData` transporta exclusivamente los valores variables obtenidos de Oracle, incluido el resultado vigente de una cancelación. Cada descarga regenera el documento para reflejar una aprobación o rechazo posterior a la emisión.

El aviso legal de los PDF se obtiene de la misma fuente contractual. React presenta su propia comunicación global mediante `DisclosureFooter`, con enlaces a `LegalInformationPage` y un cintillo regulatorio que evita confundir el prototipo con una aseguradora autorizada. Esta separación permite modificar textos web o contractuales sin alterar el controlador de descarga, la autorización por propietario ni las consultas HTTP.

## State simplificado para estados comerciales

Cotizaciones, pagos, pólizas y cancelaciones tienen transiciones permitidas. Oracle protege las reglas finales mediante restricciones y triggers; Spring valida cada operación antes de persistirla y devuelve errores de negocio comprensibles.

En cancelaciones, `CancellationService` bloquea la póliza durante la solicitud, exige el estado `ACTIVA` y rechaza otra pendiente. `AdminCancellationService` bloquea la solicitud antes de resolverla y solo permite la transición desde `SOLICITADA` hacia `APROBADA` o `RECHAZADA`. El trigger `TRG_SOLICITUD_CANCELACION_REG` conserva las reglas finales en Oracle, registra la fecha y el responsable, cambia la póliza a `CANCELACION_SOLICITADA`, `CANCELADA` o `ACTIVA` y genera su historial.

Las lecturas del cliente no replican la transición: `PolicyService`, `PanelService` y `PolicyDocumentService` proyectan el estado resultante y la última solicitud para presentarlos en API, React y PDF con labels localizados.

Se eligió una validación centralizada en lugar de una jerarquía de clases de estado porque las transiciones actuales son pocas y explícitas. Esta implementación mantiene la regla en un solo caso de uso sin agregar abstracciones innecesarias.

## Mecanismos relacionados que no se cuentan como patrones propios

- La cadena de filtros de Spring Security es infraestructura del framework; el proyecto la configura, pero no implementa una Chain of Responsibility propia.
- Los beans de Spring no constituyen por sí mismos una implementación deliberada de Singleton.
- `useState` y los eventos de React no se documentan como Observer propio.
- JPA crea proxies y factorías internamente, pero el proyecto no afirma haber implementado Factory.
- CSRF, BCrypt, cookies `HttpOnly` y validaciones son controles de seguridad, no patrones de diseño.

## Criterio para incorporar nuevos patrones

Un patrón se añadirá cuando:

1. Exista un problema repetido o una variación real que resolver.
2. Su responsabilidad pueda explicarse con archivos y pruebas concretos.
3. Reduzca acoplamiento o duplicación más de lo que aumenta complejidad.
4. Pueda mantenerse dentro del plazo del proyecto.

Después de implementarlo se actualizará este documento, `docs/ESTADO.md` y las pruebas correspondientes.
