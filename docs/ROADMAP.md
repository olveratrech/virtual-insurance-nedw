# Roadmap de desarrollo — Virtual Insurance

## Objetivo de la entrega

Entregar el **26 de noviembre de 2026** una aplicación web funcional que demuestre el recorrido comercial principal de Virtual Insurance:

1. Registro e inicio de sesión.
2. Cotización de seguro de auto o médico.
3. Resultado aprobado o rechazado.
4. Contratación de una cotización aprobada.
5. Pago simulado aprobado o rechazado.
6. Emisión y consulta de una póliza.
7. Solicitud de cancelación.

La aplicación utilizará React, Spring Boot, Spring Security y una base de datos Oracle funcional. El trabajo avanzado de administración de bases de datos que no sea necesario para esta entrega se continuará posteriormente para BDA.

## Criterio de alcance

### Incluido

- Aplicación web de escritorio adaptable a tamaños comunes de pantalla.
- Un cliente autenticado que solamente pueda consultar sus propios datos.
- Cotizaciones de auto y médicas con reglas académicas reproducibles.
- Estados explícitos para cotizaciones, pagos, pólizas y cancelaciones.
- Persistencia real en Oracle.
- API REST entre React y Spring Boot.
- Pruebas unitarias esenciales y pruebas de integración del recorrido principal.
- Datos ficticios para demostración.

### No incluido en esta entrega

- Pagos reales, facturación fiscal o cargos recurrentes.
- Biométricos, geolocalización y gestión completa de siniestros.
- Renovaciones automáticas.
- Aplicación móvil nativa.
- Infraestructura avanzada de BDA que no sea necesaria para ejecutar NEDW.

## Arquitectura prevista

```text
React
  -> servicios y adaptadores HTTP
  -> API REST de Spring Boot
  -> Controller -> Service -> Repository
  -> Oracle Database 26ai
```

Patrones implementados cuando aportan valor:

- Arquitectura por capas en el backend.
- Component Composition en React.
- Service Layer, Repository y DTO en el backend.
- Adapter entre el dominio y Spring Security.
- Provider / Context, facade de servicios y Route Guard en React.

Patrones previstos para los módulos comerciales:

- Strategy para las reglas de cotización por tipo de seguro.
- State simplificado mediante estados y transiciones validadas.

La justificación, ubicación y estado de cada patrón se mantienen en `docs/PATRONES-DISENO.md`.

## Plan de trabajo

### Fase 0 — Preparación y control del proyecto

**Fecha objetivo:** 17 de noviembre

- [x] Definir el alcance funcional prioritario.
- [x] Elaborar el anteproyecto.
- [x] Crear el prototipo navegable en Figma.
- [x] Definir la estructura inicial de carpetas.
- [x] Iniciar el repositorio Git local.
- [x] Crear el roadmap y el registro de estado.
- [x] Crear y enlazar el repositorio remoto.
- [x] Registrar la versión inicial con un primer commit.

**Entregable:** repositorio local organizado y plan verificable.

### Fase 1 — Fundamentos de React

**Fecha objetivo:** 18 de noviembre

- [x] Comprobar Node.js, npm y Git.
- [x] Crear el proyecto React con Vite dentro de `frontend/`.
- [x] Explicar `package.json`, `main.jsx`, `App.jsx` y el ciclo básico de ejecución.
- [x] Configurar estilos globales y variables visuales.
- [x] Crear el layout general, encabezado y navegación.
- [x] Configurar React Router.

**Entregable:** frontend ejecutable con rutas y layout común.

### Fase 2 — Interfaz y recorrido con datos simulados

**Fecha objetivo:** 19–20 de noviembre

- [x] Implementar inicio, acceso y panel del cliente.
- [x] Implementar formularios de cotización de auto y médica.
- [x] Implementar resultados aprobado y rechazado.
- [x] Implementar pago, detalle de póliza y cancelación.
- [x] Crear componentes reutilizables.
- [x] Crear servicios y adaptadores temporales con datos simulados.
- [x] Validar campos y mostrar errores comprensibles.
- [x] Conservar los datos al volver desde una cotización rechazada.
- [x] Conservar la cotización al volver desde pago.
- [x] Mantener el tipo de seguro durante toda la navegación.
- [x] Validar fechas pasadas y vencimientos de demostración.
- [x] Mostrar una página no encontrada para pólizas inexistentes.

**Entregable:** recorrido completo navegable en React, todavía sin backend real.

### Fase 3 — Base de datos mínima para NEDW

**Fecha objetivo:** 20–21 de noviembre

- [x] Cerrar las entidades y atributos necesarios para el recorrido prioritario.
- [x] Definir estados y transiciones de cotización, pago, póliza y cancelación.
- [x] Preparar tipos de datos, PK, FK, `UNIQUE`, `CHECK` e índices esenciales para Oracle.
- [x] Crear scripts reproducibles de esquema y datos ficticios.
- [x] Ejecutar y verificar los scripts en Oracle Database 26ai.
- [x] Documentar decisiones que se terminarán posteriormente en BDA.

**Entregable:** esquema Oracle reproducible y suficiente para la aplicación web.

### Fase 4 — Backend y seguridad

**Fecha objetivo:** 21–23 de noviembre

- [x] Crear el proyecto Spring Boot dentro de `backend/`.
- [x] Configurar la conexión con Oracle.
- [x] Crear entidades, DTO, repositorios, servicios y controladores para los módulos integrados.
- [x] Implementar Strategy para cotizaciones de auto y médicas.
- [x] Implementar transiciones válidas de estados.
- [x] Rechazar solicitudes de cancelación duplicadas cuando ya exista una pendiente.
- [x] Configurar registro, autenticación y autorización inicial con Spring Security.
- [x] Exponer endpoints del recorrido prioritario hasta pólizas y documentos.
- [x] Documentar el contrato básico de la API.

**Entregable:** API REST autenticada y conectada con Oracle.

### Fase 5 — Integración completa

**Fecha objetivo:** 23–24 de noviembre

- [x] Sustituir los mocks restantes de cancelaciones por llamadas HTTP.
- [x] Sustituir la actividad simulada del panel por llamadas HTTP.
- [x] Integrar inicio de sesión, persistencia de sesión y logout.
- [x] Integrar el registro ampliado con fotografía opcional y catálogos reales.
- [x] Integrar ambas cotizaciones y sus resultados.
- [x] Integrar pago simulado y emisión de póliza.
- [x] Integrar consulta de pólizas y descarga protegida de documentos PDF.
- [x] Integrar consulta y solicitud de cancelación.
- [x] Comprobar que un cliente no pueda consultar cotizaciones, pólizas ni documentos de otro.

**Entregable:** recorrido funcional React–Spring Boot–Oracle.

### Fase 5.1 — Administración de cancelaciones

**Fecha objetivo:** antes de la estabilización final

- [x] Fijar la fecha efectiva de cancelación al día actual y rechazar fechas distintas en la API.
- [x] Crear usuarios internos, roles y auditoría mínima de resolución en Oracle.
- [x] Implementar autenticación y autorización administrativa en Spring Security.
- [x] Implementar la API de consulta, aprobación y rechazo de cancelaciones.
- [x] Construir el panel administrativo en React.
- [x] Integrar y verificar el resultado administrativo en póliza, PDF, panel e historial del cliente.

**Entregable:** ciclo completo de solicitud y resolución administrativa de cancelaciones.

### Fase 5.2 — Identidad visual y documentos

**Fecha objetivo:** antes de la estabilización final

- [x] Incorporar logotipo e isotipo propios en los encabezados del cliente y administración.
- [x] Configurar el favicon propio de la aplicación.
- [x] Integrar el logotipo institucional en la póliza y el comprobante PDF.
- [x] Verificar la marca en escritorio, vista estrecha y documentos descargados.
- [x] Rediseñar la plantilla completa de la póliza y ampliar su contenido contractual.
- [x] Incorporar tipografías embebidas y estados con color semántico en la póliza.
- [x] Unificar el aviso explícito sobre el carácter ficticio y educativo de la aplicación.

**Entregable:** identidad y comunicación compartidas por React y documentos PDF, con póliza y comprobante profesionalizados.

### Fase 5.3 — Endurecimiento de contratación y pago

**Fecha objetivo:** antes de la estabilización final

- [x] Incorporar selección y validación cruzada de Visa, Mastercard y American Express.
- [x] Habilitar instrumentos controlados y repetibles para las tres redes.
- [ ] Impedir visualmente un nuevo pago cuando la cotización ya produjo una póliza.
- [ ] Verificar en API que una cotización pagada no pueda generar otro intento aprobado ni otra póliza.
- [ ] Mantener el bloqueo aunque la póliza vinculada esté cancelada.
- [ ] Cubrir navegación normal, recarga y acceso directo a la ruta de pago.

**Entregable parcial:** redes de tarjeta completas; pendiente cerrar definitivamente la reutilización de cotizaciones pagadas.

### Fase 6 — Pruebas, estabilización y entrega

**Fecha objetivo:** 24–26 de noviembre

- [ ] Ejecutar pruebas unitarias de reglas críticas.
- [x] Ejecutar pruebas de integración del backend y Oracle hasta el incremento de cancelaciones.
- [x] Probar manualmente los recorridos principales y de error implementados.
- [x] Corregir los defectos bloqueantes y de alta prioridad detectados hasta el incremento de cancelaciones.
- [ ] Preparar datos y pasos reproducibles para la demostración.
- [x] Mantener actualizados README, estado del proyecto y evidencias durante los incrementos.
- [ ] Crear una versión o etiqueta de entrega.

**Entregable:** proyecto demostrable, documentado y reproducible.

## Escenarios mínimos de aceptación

- Registro e inicio de sesión correctos e incorrectos.
- Cotización de auto aprobada.
- Cotización médica rechazada.
- Pago aprobado que genera una póliza activa.
- Pago rechazado que no genera ni activa una póliza.
- Consulta de cotizaciones, pagos y pólizas del cliente autenticado.
- Solicitud de cancelación que cambia al estado correspondiente sin cancelar inmediatamente.
- Validación de entradas incompletas o inválidas.
- Rechazo de accesos a recursos de otro cliente.

## Regla de actualización

Al terminar una sesión de trabajo:

1. Marcar aquí únicamente los hitos concluidos.
2. Actualizar `docs/ESTADO.md` con lo implementado, lo parcial y lo pendiente.
3. Ejecutar las pruebas relacionadas con el cambio.
4. Crear un commit pequeño con un mensaje descriptivo.

