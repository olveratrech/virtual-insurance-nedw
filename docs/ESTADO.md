# Estado del proyecto — Virtual Insurance

**Última actualización:** 20 de noviembre de 2026
**Entrega de NEDW:** 26 de noviembre de 2026  
**Responsable:** Emmanuel Trejo Olvera

## Resumen ejecutivo

- El alcance prioritario de NEDW está definido.
- El anteproyecto y el prototipo navegable existen.
- El modelo relacional reducido para NEDW y el contrato inicial de la API están definidos.
- El entorno Oracle Database 26ai está disponible en Docker.
- El recorrido principal de React funciona con datos simulados; Spring Boot y la integración con Oracle todavía no han comenzado.

## Estado por área

| Área | Estado | Observaciones |
|---|---|---|
| Anteproyecto | Terminado | Documento base disponible en `anteproyecto/`. |
| Prototipo Figma | Terminado con revisión pendiente | Diez pantallas y recorridos principales navegables. |
| Modelo relacional | Aprobado para NEDW | Tablas, relaciones, cardinalidades, estados, pagos y cancelaciones definidos en `docs/MODELO-RELACIONAL-NEDW.md`. |
| Contrato API | Definido | Entradas, salidas, errores, autenticación y transacciones documentados en `docs/CONTRATO-API.md`. |
| Oracle | Clientes y tarjetas verificados | `CLIENTE` y `TARJETA` desplegadas; repetibilidad, relaciones, índices, BCrypt y fotografía BLOB comprobados. |
| Frontend React | Recorrido principal completo con mocks | Falta adaptarlo al registro ampliado y sustituir simulaciones por servicios HTTP. |
| Backend Spring Boot | No iniciado | Modelo y contrato REST definidos antes de generar el proyecto. |
| Spring Security | Decidido, no implementado | Usará sesión de servidor, cookie `HttpOnly` y protección CSRF. |
| Integración completa | No iniciada | Depende del frontend, API y esquema Oracle. |
| Pruebas | Parciales | El incremento de catálogos cuenta con pruebas SQL aprobadas; faltan entidades transaccionales y pruebas de aplicación. |
| Repositorio Git | Configurado | Rama `main` sincronizada con `origin/main` en GitHub. |

## Componentes disponibles

### Documentación

- `README.md`: presentación general del repositorio.
- `RESUMEN-ASPECTOS-GENERALES.md`: diagnóstico previo del proyecto.
- `docs/ROADMAP.md`: fases, entregables y fechas objetivo.
- `docs/ESTADO.md`: registro vivo del avance.
- `docs/MODELO-RELACIONAL-NEDW.md`: tablas, relaciones, cardinalidades y reglas del modelo aprobado.
- `docs/CONTRATO-API.md`: contrato entre React y Spring Boot.
- `anteproyecto/`: documentación entregable del anteproyecto.

### Diseño de interfaz

- Archivo Figma: **Virtual Insurance — Mockup navegable NEDW**.
- Pantallas disponibles:
  - Inicio.
  - Acceso y registro.
  - Panel del cliente.
  - Cotización de auto.
  - Cotización médica.
  - Resultado aprobado.
  - Resultado rechazado.
  - Contratación y pago.
  - Detalle de póliza.
  - Solicitud de cancelación.

#### Pendientes del diseño

- Completar los enlaces y recorridos secundarios que todavía no están conectados.
- Revisar textos finales y consistencia visual antes de implementar cada pantalla.
- Definir comportamiento adaptable para pantallas pequeñas.
- Precisar estados de carga, errores de red y sesión expirada.
- Agregar, si se requiere, vistas de historial detallado.

### Frontend React

- Proyecto creado con React y Vite.
- React Router configurado con las diez rutas iniciales y una ruta de página no encontrada.
- `MainLayout` comparte el encabezado y el área de contenido entre páginas.
- `Header` implementa identidad, navegación principal y acceso.
- `HomePage` implementa la primera versión funcional de la pantalla de inicio.
- `InsuranceCard` reutiliza una misma estructura para seguros de auto y médicos mediante propiedades.
- `NotFoundPage` atiende rutas y pólizas inexistentes; todas las rutas principales tienen pantalla propia.
- `AccessPage` implementa acceso y registro con formularios separados.
- `LoginForm` y `RegisterForm` administran valores y validaciones locales mediante `useState`.
- `FormField` unifica etiquetas, campos y mensajes de error accesibles.
- El envío correcto navega al panel, pero la autenticación continúa siendo simulada y no persiste usuarios.
- `DashboardPage` implementa el panel del cliente con saludo, accesos rápidos y actividad reciente.
- `DashboardActionCard`, `ActivityList` y `StatusBadge` presentan acciones y estados reutilizables.
- `dashboardData.js` separa los datos ficticios de los componentes que los presentan.
- `QuotePageLayout` comparte la estructura visual y el aviso demostrativo de ambas cotizaciones.
- `AutoQuoteForm` captura vehículo, licencia, edad, puntos negativos y antecedentes declarados.
- `MedicalQuoteForm` captura edad y cinco factores declarados de salud.
- `SelectField` y `CheckboxField` complementan los controles reutilizables del formulario.
- `quoteRules.js` aísla reglas académicas provisionales de aprobación y rechazo para cada seguro.
- `QuoteResultPage` presenta resultados aprobados y rechazados mediante renderizado condicional.
- `quoteResultData.js` mantiene precios, coberturas, beneficios y mensajes ficticios fuera de la interfaz.
- Una cotización aprobada puede continuar a contratación; una rechazada permite revisar los datos sin crear póliza ni cargo.
- `PaymentPage` implementa contratación y pago simulado con resumen separado del formulario.
- `PaymentForm` valida vigencia, modalidad, tarjeta ficticia, vencimiento, CVV y términos.
- `paymentRules.js` permite demostrar pagos aprobados y rechazados sin procesar cargos reales.
- Un pago rechazado permanece en la pantalla y no navega hacia una póliza; solamente uno aprobado continúa.
- `PolicyPage` implementa el detalle de póliza mediante el parámetro dinámico `:polizaId`.
- `PolicyDetails`, `PaymentOverview` y `DocumentList` separan información contractual, pagos y documentos.
- `policyData.js` contiene pólizas ficticias diferenciadas para auto y seguro médico.
- La descarga de documentos está simulada y no genera todavía archivos PDF reales.
- `CancellationPage` implementa la solicitud y la confirmación sin cancelar inmediatamente la cobertura.
- `CancellationForm` valida motivo, fecha, comentarios y aceptación del proceso.
- `cancellationRules.js` genera un folio y el estado temporal `CANCELLATION_REQUESTED`.
- Al regresar, `PolicyPage` muestra `CANCELACIÓN SOLICITADA` y conserva la indicación de cobertura activa.
- `PolicyPage` oculta la acción de cancelación cuando ya existe una solicitud pendiente.
- Variables visuales, estilos globales y adaptación inicial para pantallas pequeñas configurados.
- `npm run lint` y `npm run build` terminan correctamente.
- La página de inicio, el acceso, el registro y sus validaciones aprobaron la revisión visual y las pruebas manuales del autor.
- El panel, sus accesos rápidos, la actividad reciente y su adaptación a una columna aprobaron la revisión visual y las pruebas manuales del autor.
- Los formularios de auto y médico, sus validaciones, reglas provisionales y recorridos de aprobación y rechazo aprobaron la revisión visual y las pruebas manuales del autor.
- Las páginas de resultado aprobado y rechazado de ambos seguros aprobaron la revisión visual y las pruebas manuales del autor.
- La contratación, las validaciones y los pagos simulados aprobado y rechazado aprobaron la revisión visual y las pruebas manuales del autor.
- Las pólizas de auto y médica, pagos, documentos simulados y diseño adaptable aprobaron la revisión visual y las pruebas manuales del autor.
- La cancelación de ambos tipos de póliza, su estado intermedio y la prevención visual de solicitudes duplicadas aprobaron la revisión visual y las pruebas manuales del autor.

#### Pendientes del frontend

- Completar refinamientos de UX, estados de carga y errores antes de sustituir los mocks.
- Incorporar estados de carga, validación y error.
- Sustituir posteriormente los datos simulados por servicios HTTP.
- Conectar acceso y registro con Spring Security y eliminar la simulación local.
- Sustituir `quoteRules.js` por estrategias del backend y reglas definitivas documentadas.
- Generar pólizas y comprobantes PDF reales desde el backend.
- Validar en el backend que una póliza con cancelación pendiente no acepte solicitudes duplicadas; la interfaz ya oculta la acción, pero no constituye una medida de seguridad.
- Revisar visualmente la aplicación en el navegador y comparar con Figma.
- Los valores capturados se conservan cuando el usuario selecciona “Revisar datos” desde una cotización rechazada.

## Auditoría del recorrido frontend

### Correcciones prioritarias antes del backend

- Corrección terminada: conservar los campos capturados al utilizar “Revisar datos” desde un rechazo.
- Corrección terminada: conservar el tipo de seguro y la cotización al volver desde pago.
- Corrección terminada: rechazar fechas pasadas de vigencia y cancelación, y vencimientos ficticios expirados.
- Corrección terminada: mostrar recurso no encontrado cuando `:polizaId` no exista, sin utilizar una póliza de respaldo.

### Pendientes que dependen de integración

- Proteger panel, cotizaciones, pagos y pólizas mediante autenticación real.
- Reflejar la sesión en el encabezado y permitir cerrar sesión.
- Persistir el estado después de recargar el navegador.
- Agregar estados de carga, errores HTTP y sesión expirada.
- Sustituir los archivos de `mocks/` por servicios y adaptadores HTTP cuando corresponda.
- Impedir desde el backend accesos a pólizas de otros clientes y operaciones duplicadas.
- Generar y descargar documentos reales.

### Calidad pendiente

- Incorporar pruebas automatizadas de reglas, validaciones y navegación crítica.
- Eliminar recursos demostrativos de Vite que ya no se utilizan.
- Corrección terminada: `NotFoundPage` sustituyó a `ComingSoonPage` como página 404 definitiva.
- Revisar el encabezado móvil, accesibilidad y consistencia final contra Figma.

### Modelo relacional

- Modelo lógico para NEDW aprobado a partir del diagrama Mermaid revisado por el autor.
- Conserva género, sexo, tarjetas ficticias y tipos de tarjeta.
- Sustituye pago por intento de pago y ubica la emisión de póliza después de un intento aprobado.
- Obtiene la cotización de una póliza por medio del intento de pago, sin una segunda relación directa.
- Incorpora estados e historial de póliza, solicitudes de cancelación y documentos.
- Excluye biometría, siniestros y drogas del alcance NEDW.

#### Pendientes del modelo físico

- Traducir tipos de ER/Studio a tipos apropiados de Oracle Database 26ai.
- Definir columnas identidad, índices y restricciones.
- Diseñar el cifrado de números de tarjeta sin persistir CVV.
- Implementar reglas que involucran varias tablas.
- Preparar DDL, catálogos, datos ficticios y pruebas reproducibles.

### Entorno Oracle

- Oracle Database 26ai está instalado en Oracle Linux dentro de Docker.
- La CDB `FREE` y la PDB `ETOBDA_S1` están abiertas y verificadas.
- El servicio `etobda_s1.fi.unam` es accesible desde macOS mediante `localhost:1522`.

#### Pendientes de Oracle para NEDW

- Desplegar el esquema mínimo de la aplicación.
- Ejecutar y verificar el incremento de catálogos desde SQL Developer en VS Code.
- Probar restricciones y operaciones principales.
- Documentar comandos reproducibles de creación y carga.

## Componentes todavía no iniciados

### Backend

- Proyecto Spring Boot.
- Conexión con Oracle.
- Entidades y DTO.
- Repositorios, servicios y controladores.
- Strategy de cotización.
- Reglas de transición de estados.
- Spring Security.

### Pruebas

- Pruebas unitarias de cotización y estados.
- Pruebas de repositorios y servicios.
- Pruebas de integración con Oracle.
- Pruebas del frontend.
- Guion manual de demostración de extremo a extremo.

## Decisiones vigentes

- La entrega de noviembre prioriza NEDW y utiliza únicamente la base de datos necesaria para que la aplicación funcione.
- El proyecto de BDA se continuará después con mayor profundidad administrativa y física.
- El frontend se construirá primero con datos simulados y contratos estables.
- No se integrarán pagos reales.
- Se implementarán patrones solamente cuando resuelvan necesidades concretas.
- La aplicación se desarrollará individualmente.
- La autenticación usará sesiones administradas por Spring Security mediante cookie `HttpOnly` y protección CSRF.
- La API utilizará DTO explícitos y no devolverá entidades JPA directamente.
- Solo se admitirán números de tarjeta ficticios documentados; el número se almacenará cifrado y el CVV nunca se persistirá.
- `docs/ESTADO.md` será el registro operativo y la base de contexto entre fases.
- Antes de implementar una sección se explicarán sus componentes, archivos, responsabilidades y relaciones para permitir que el autor pueda modificarlos con autonomía.

## Herramientas verificadas

- Node.js `24.16.0`.
- npm `11.13.0`.
- OpenJDK `25.0.4.1`.
- Maven `3.9.16`.
- Repositorio remoto: `https://github.com/olveratrech/virtual-insurance-nedw.git`.
- Rama principal: `main`.
- Primer commit publicado: `f3a07a1` (`first commit`).
- Vite `8.3.8` y React `19.3.1` instalados en `frontend/`.

## Incidencias técnicas abiertas

- `npm audit` reporta cinco vulnerabilidades altas originadas por `picomatch 4.0.7`, dependencia transitiva de las herramientas de desarrollo de Vite.
- `npm audit fix` sin `--force` no puede resolverlas; la propuesta automática requiere bajar a Vite 6 y se evaluará antes del primer commit del frontend.
- No se ejecutará `npm audit fix --force` sin revisar previamente el impacto.

## Riesgos actuales

- Plazo corto para aprender React e integrar cuatro tecnologías.
- Cambios tardíos en el modelo relacional.
- Intentar cubrir módulos de siniestros que no son esenciales para la demostración comercial.
- Dedicar demasiado tiempo al diseño visual antes de lograr el recorrido funcional.
- Dejar seguridad y pruebas para el final.

## Próxima acción

Preparar cotizaciones, subtipos, historial de estados y sus pruebas reproducibles.

## Bitácora breve

| Fecha | Cambio | Resultado | Pendientes detectados |
|---|---|---|---|
| 17/11/2026 | Se definió la estructura del repositorio y se crearon los documentos de control. | Preparación inicial concluida. | Crear remoto y comenzar React. |
| 17/11/2026 | Se verificaron el primer commit, el remoto de GitHub y las herramientas de desarrollo. También se revisó el prototipo de Figma. | Repositorio remoto operativo y entorno listo para React. | Completar enlaces del prototipo y aprobar la estructura del primer incremento frontend. |
| 17/11/2026 | Se creó la plantilla React con Vite y se instalaron sus dependencias. | `npm run lint` y `npm run build` finalizan correctamente. | Implementar rutas y layout; decidir el tratamiento de la alerta de `picomatch`. |
| 17/11/2026 | Se implementaron rutas, layout compartido, encabezado, inicio, tarjetas reutilizables y marcadores de trabajo pendiente. | Primer incremento React compila y pasa el analizador estático. | Revisión visual e implementación posterior de acceso y registro. |
| 17/11/2026 | Se implementaron acceso, registro, campos reutilizables y validación local. | La ruta `/acceso` compila y navega al panel tras una simulación válida. | Sustituir la simulación por Spring Security e implementar el panel. |
| 17/11/2026 | El autor realizó la revisión visual y las pruebas manuales de acceso y registro. | Campos obligatorios, contraseñas, términos y navegación funcionan conforme al incremento actual. | Registrar el incremento en Git y continuar con el panel del cliente. |
| 17/11/2026 | Se implementó el panel con acciones de cotización, estados y actividad reciente basada en mocks. | La ruta `/panel` compila y los accesos enlazan con las rutas planificadas. | Revisión visual y formularios de cotización. |
| 17/11/2026 | El autor probó el acceso al panel, acciones, actividad reciente y diseño adaptable. | El incremento del panel funciona conforme al alcance actual. | Registrar el incremento en Git y continuar con cotizaciones. |
| 17/11/2026 | Se implementaron formularios de auto y médico con controles compartidos y reglas provisionales aisladas. | Ambos formularios compilan, validan entradas y navegan hacia aprobación o rechazo. | Revisión manual y páginas de resultado. |
| 17/11/2026 | El autor probó ambos formularios, valores límite y decisiones aprobadas y rechazadas. | Las validaciones, adaptación visual y navegación funcionan conforme a las reglas provisionales. | Registrar el incremento y construir las páginas de resultado. |
| 17/11/2026 | Se implementó una página configurable para resultados aprobados y rechazados. | Muestra producto, cobertura y precio cuando procede; el rechazo no genera operaciones. | Revisión manual y contratación/pago. |
| 17/11/2026 | El autor probó los cuatro resultados, acciones y diseño visual. | Aprobación, rechazo y navegación funcionan conforme al alcance actual. | Preservar datos al revisar una cotización rechazada y registrar el incremento. |
| 17/11/2026 | Se implementó contratación y pago simulado con tarjetas de demostración. | Un pago aprobado continúa a póliza y uno rechazado permanece sin emitirla. | Revisión manual y detalle de póliza. |
| 18/11/2026 | El autor probó validaciones, pago aprobado, pago rechazado y adaptación visual. | El flujo impide emitir una póliza cuando el simulador rechaza el pago. | Registrar el incremento y construir el detalle de póliza. |
| 18/11/2026 | Se implementó el detalle de póliza para auto y médico, pagos y documentos simulados. | La ruta dinámica muestra la póliza correspondiente y conserva únicamente el resultado del pago. | Revisión manual y solicitud de cancelación. |
| 18/11/2026 | El autor probó ambas pólizas, emisión posterior al pago, documentos y adaptación visual. | El detalle funciona conforme al alcance actual y no expone datos de tarjeta. | Registrar el incremento y construir cancelación. |
| 18/11/2026 | Se implementó solicitud de cancelación con estado intermedio y folio simulado. | La póliza permanece activa y muestra la cancelación en revisión. | Revisión manual y auditoría del recorrido frontend. |
| 18/11/2026 | Se corrigió la acción disponible después de solicitar cancelación. | La póliza ya no permite iniciar otra solicitud desde la interfaz mientras la anterior está pendiente. | Replicar la validación obligatoria en Spring Boot. |
| 18/11/2026 | El autor verificó cancelación en pólizas de auto y médicas, incluido el intento duplicado. | El botón desaparece y el recorrido no permite iniciar otra solicitud desde la interfaz. | Registrar el incremento y auditar el recorrido frontend completo. |
| 18/11/2026 | Se auditó el recorrido frontend completo y las diez rutas principales. | Se separaron fallos locales, pendientes de integración y mejoras de calidad. | Corregir navegación y validaciones prioritarias antes del backend. |
| 18/11/2026 | Se conservaron los valores al regresar desde una cotización rechazada. | Auto y médico restauran campos y selecciones durante la navegación; el autor verificó ambos recorridos. | Corregir el regreso desde pago sin perder el tipo de seguro. |
| 18/11/2026 | Se conservó la cotización y su tipo al volver desde pago. | Auto regresa a auto, médico regresa a médico y cada pago aprobado emite la póliza ficticia correspondiente; el autor verificó los recorridos. | Validar fechas pasadas y vencimientos ficticios. |
| 18/11/2026 | Se centralizaron las validaciones de fechas y vencimiento ficticio. | Vigencia y cancelación rechazan fechas pasadas; la tarjeta rechaza meses expirados; el autor verificó los límites. | Manejar identificadores de póliza inexistentes. |
| 18/11/2026 | Se agregó una página 404 y se eliminaron respaldos silenciosos para pólizas desconocidas. | Rutas, pólizas y cancelaciones inexistentes muestran un error explícito; el autor verificó rutas válidas e inválidas. | Registrar la corrección y preparar la persistencia mínima. |
| 19/11/2026 | Se aprobó el modelo relacional reducido para NEDW. | Quedaron definidas las tablas, relaciones, cardinalidades, estados, pagos, cancelaciones y tratamiento de tarjetas ficticias. | Traducir el modelo aprobado a DDL de Oracle. |
| 19/11/2026 | Se definió el contrato REST inicial entre React y Spring Boot. | Se documentaron autenticación, catálogos, registro, cotizaciones, intentos de pago, pólizas, documentos, cancelaciones, errores y transacciones. | Revisar el contrato y crear el DDL reproducible. |
| 19/11/2026 | Se preparó el primer incremento de Oracle. | Se documentó la estructura reproducible y se creó el script administrativo no destructivo para `VI_APP`. | Ejecutar el script, probar la conexión y registrar el resultado antes de crear tablas. |
| 19/11/2026 | Se ejecutó el script administrativo en `ETOBDA_S1`. | El usuario local `VI_APP` quedó creado, abierto y con `USERS` como tablespace predeterminado. | Verificar conexión, cuota y privilegios efectivos antes del primer DDL. |
| 19/11/2026 | Se verificó la sesión de `VI_APP`. | La conexión a `ETOBDA_S1`, la cuota de `USERS` y los privilegios mínimos son correctos. | Crear y probar las tablas de catálogo. |
| 19/11/2026 | Se preparó el incremento de catálogos. | Siete tablas, datos iniciales repetibles y pruebas de estructura quedaron documentados en scripts separados. | Ejecutar los tres scripts desde la conexión `VI_APP@ETOBDA_S1`. |
| 19/11/2026 | La primera ejecución de catálogos devolvió `ORA-01031`. | Se identificó la falta de `create sequence`, requerido por las columnas `identity`; no se creó la primera tabla. | Conceder y verificar el privilegio, después repetir el script de estructura. |
| 20/11/2026 | Se concedió `create sequence` y se repitió el incremento de catálogos. | La creación repetible, la carga mediante `merge` y todas las pruebas de tablas, columnas, identidades, restricciones y conteos finalizaron correctamente. | Registrar el incremento y continuar con clientes y tarjetas. |
| 20/11/2026 | Se preparó el incremento de clientes y tarjetas. | Estructura repetible, índices de FK, cliente BCrypt, fotografía BLOB y estrategia AES-GCM quedaron definidos con sus pruebas. | Ejecutar dos veces estructura y carga, y después las pruebas. |
| 20/11/2026 | Se ejecutó y verificó el incremento de clientes y tarjetas. | La segunda ejecución conservó los objetos, la carga actualizó sin duplicar y las pruebas confirmaron relaciones, índices, hash y fotografía de 70 bytes. | Registrar el incremento y continuar con cotizaciones. |

