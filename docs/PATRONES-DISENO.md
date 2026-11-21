# Patrones de diseño y arquitectura

Este documento registra los patrones utilizados deliberadamente en Virtual Insurance, el problema que resuelven y los archivos donde pueden verificarse. No se busca acumular patrones: cada uno debe reducir acoplamiento, concentrar una responsabilidad o facilitar pruebas y mantenimiento.

Última actualización: **20 de noviembre de 2026**.

## Resumen

| Patrón o enfoque | Capa | Estado | Propósito principal |
|---|---|---|---|
| Arquitectura por capas | Backend | Implementado | Separar HTTP, negocio, persistencia y base de datos |
| MVC | Backend | Implementado mediante Spring MVC | Separar controladores, modelos de entrada/salida y servicios |
| Repository | Backend | Implementado | Abstraer persistencia y consultas JPA |
| Service Layer | Backend | Implementado | Concentrar reglas, coordinación y transacciones |
| DTO | Backend | Implementado | Definir contratos sin exponer entidades JPA |
| Adapter | Backend | Implementado | Adaptar el cliente del dominio a Spring Security |
| Dependency Injection | Backend | Implementado mediante Spring | Desacoplar dependencias y facilitar pruebas |
| Component Composition | Frontend | Implementado | Construir pantallas mediante componentes reutilizables |
| Provider / Context | Frontend | Implementado | Compartir la sesión autenticada sin prop drilling |
| Facade de servicios | Frontend | Implementado | Ocultar `fetch`, cookies, CSRF, multipart y rutas HTTP |
| Route Guard | Frontend | Implementado | Impedir acceso visual a rutas que requieren sesión |
| Strategy | Backend | Implementado | Separar reglas de cotización de auto y médica |
| Ports and Adapters | Backend | Implementado | Aislar el caso de uso de pago de la pasarela académica |
| State simplificado | Backend y Oracle | Planificado | Controlar transiciones válidas de estados comerciales |

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
  -> AuthenticationManager
  -> ClientUserDetailsService (Adapter)
  -> ClientRepository
  -> CLIENTE
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

**Problema:** Spring Security autentica mediante `UserDetailsService`, pero el modelo propio utiliza `Client` y `ClientRepository`.

**Aplicación:** `ClientUserDetailsService` traduce un cliente persistido a `ClientPrincipal`, que implementa el contrato `UserDetails`.

**Beneficio:** la entidad `Client` no depende de Spring Security y el mecanismo de autenticación puede cambiar sin rediseñar la tabla.

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

**Aplicación:** `AuthProvider` conserva el cliente y el estado de sesión; `useAuth` proporciona acceso controlado a login, registro, logout y recuperación de sesión.

**Archivos:**

- `frontend/src/contexts/AuthContext.jsx`
- `frontend/src/contexts/authContext.js`
- `frontend/src/hooks/useAuth.js`

**Beneficio:** se evita pasar datos de autenticación manualmente a través de componentes intermedios.

### Facade de servicios HTTP

**Problema:** los componentes no deben repetir `fetch`, cookies, CSRF, serialización multipart ni interpretación de `ProblemDetail`.

**Aplicación:**

- `apiClient.js` maneja transporte y errores comunes.
- `authApi.js` expone operaciones de autenticación y oculta CSRF y multipart.
- `catalogApi.js` expone la lectura de catálogos.

**Beneficio:** los componentes llaman operaciones como `login`, `register` o `getGenders` sin conocer detalles del protocolo.

### Route Guard

**Problema:** una persona sin sesión no debe visualizar el panel del cliente.

**Aplicación:** `ProtectedRoute` consulta el contexto, muestra el estado de carga, maneja errores de conexión y redirige a `/acceso` cuando no existe sesión.

**Límite:** esta protección mejora la navegación, pero no sustituye la autorización del backend. Spring debe validar siempre al propietario del recurso.

## Strategy para cotizaciones

Auto y médico comparten el caso de uso de cotización, pero tienen datos y reglas diferentes. Se implementó una estrategia por producto detrás de un servicio coordinador común.

`QuoteStrategy<T>` define la evaluación común. `AutoQuoteStrategy` y `MedicalQuoteStrategy` producen una `QuoteDecision` sin depender de HTTP, sesión o persistencia; `QuoteService` coordina la transacción y el subtipo. Las pruebas cubren decisiones aprobadas y rechazadas de ambas estrategias.

No se crean estrategias adicionales para reglas idénticas.

## Puerto y adaptador de pagos

`PaymentGateway` es el puerto requerido por el caso de uso y `SimulatedPaymentGateway` es el adaptador que consulta el procesador ficticio en Oracle. `PaymentService` depende del contrato, no de la implementación concreta, por lo que una pasarela externa podría sustituir al simulador sin mover sus detalles al controlador ni al frontend.

La abstracción no oculta la transacción: el servicio coordina idempotencia, intento, débito, póliza, historial y documentos. Las respuestas del procesador se representan con `GatewayPaymentResult` y nunca exponen el número completo ni el CVV.

## Patrones planificados

### State simplificado para estados comerciales

Cotizaciones, pagos, pólizas y cancelaciones tienen transiciones permitidas. Oracle ya protege varias reglas mediante restricciones y triggers; Spring deberá validar las operaciones antes de persistirlas.

Se utilizará una tabla explícita de transiciones, enums con comportamiento o clases de estado únicamente si reducen condicionales repetidos. No se implementará una jerarquía compleja si una validación centralizada resulta suficiente.

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
