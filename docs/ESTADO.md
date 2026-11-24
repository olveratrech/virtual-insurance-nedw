# Estado del proyecto — Virtual Insurance

**Última actualización:** 24 de noviembre de 2026
**Entrega de NEDW:** 26 de noviembre de 2026  
**Responsable:** Emmanuel Trejo Olvera

## Resumen ejecutivo

- El alcance prioritario de NEDW está definido.
- El anteproyecto y el prototipo navegable existen.
- El modelo relacional reducido para NEDW y el contrato inicial de la API están definidos.
- El esquema funcional está desplegado y probado en Oracle Database 26ai dentro de Docker.
- Registro, autenticación, cotizaciones, pago académico y emisión de póliza ya recorren React, Spring Boot y Oracle.
- El portal del cliente cuenta con un shell institucional responsivo, navegación por módulos, historiales filtrables, formularios endurecidos y un footer regulatorio académico.

## Estado por área

| Área | Estado | Observaciones |
|---|---|---|
| Anteproyecto | Terminado | Documento base disponible en `anteproyecto/`. |
| Prototipo Figma | Referencia terminada | Diez pantallas y recorridos principales navegables; la implementación React evolucionó y fue validada como fuente visual vigente. |
| Modelo relacional | Administración incorporada | Usuarios internos, roles y responsabilidad de resolución complementan el modelo comercial aprobado. |
| Contrato API | Definido | Entradas, salidas, errores, autenticación y transacciones documentados en `docs/CONTRATO-API.md`. |
| Oracle | Administración verificada | Siete incrementos, incluido el modelo administrativo de cancelaciones, están desplegados y probados de forma repetible. |
| Frontend React | Portal institucional verificado | Acceso dividido, shell responsivo, cotización y pago rediseñados, historiales filtrables, perfil reorganizado, ayuda y footer global aprobaron la validación final. |
| Backend Spring Boot | Perfil protegido verificado | Las 100 pruebas pasan; los cambios de correo exigen contraseña y los datos contractuales permanecen inmutables. |
| Spring Security | Autenticación dual verificada | Clientes y usuarios internos tienen proveedores, principales y roles separados; CSRF, BCrypt, sesión, login, logout y aislamiento de rutas fueron verificados. |
| Integración completa | Cancelaciones completas | Solicitud, revisión administrativa, resolución y consulta del resultado recorren React, Spring Boot y Oracle. |
| Pruebas | Cierre técnico aprobado | 100 pruebas Spring, E2E Oracle, 8 pruebas React, `lint`, `build` y validación manual están aprobados. |
| Repositorio Git | Configurado | Rama `main` sincronizada con `origin/main` en GitHub. |

## Componentes disponibles

### Documentación

- `README.md`: presentación general del repositorio.
- `RESUMEN-ASPECTOS-GENERALES.md`: diagnóstico previo del proyecto.
- `docs/ROADMAP.md`: fases, entregables y fechas objetivo.
- `docs/ESTADO.md`: registro vivo del avance.
- `docs/MODELO-RELACIONAL-NEDW.md`: tablas, relaciones, cardinalidades y reglas del modelo aprobado.
- `docs/CONTRATO-API.md`: contrato entre React y Spring Boot.
- `docs/PATRONES-DISENO.md`: patrones implementados, planificados y criterio de incorporación.
- `docs/GUIA-DEMO.md`: preparación de seeds, credenciales ficticias, recorridos y verificación responsiva de la demostración.
- `docs/ENTREGA.md`: checkpoint, comandos de verificación, revisión manual, commit y etiqueta propuesta para el cierre.
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

#### Cierre del diseño

- Los enlaces incluidos en el alcance, textos, estados de carga y error, vistas de historial y comportamiento adaptable están implementados y validados.
- Los recorridos secundarios excluidos se enumeran en `Alcance descartado para la entrega` y no constituyen pendientes del producto entregado.

### Frontend React

- Proyecto creado con React y Vite.
- React Router configurado con las diez rutas iniciales y una ruta de página no encontrada.
- `MainLayout` separa la navegación pública del shell autenticado del cliente.
- `PortalShell` presenta sidebar institucional fija desde 1024 px y topbar con isotipo y drawer en pantallas menores; el drawer cierra por ruta, fondo o tecla Escape.
- El shell utiliza el logotipo oficial completo en escritorio y el isotipo oficial en móvil.
- `DisclosureFooter` implementa tres columnas institucionales, enlaces de producto, atención simulada y un cintillo regulatorio explícito sobre UNAM, CNSF y CONDUSEF.
- `LegalInformationPage` atiende `/condiciones-generales` y `/aviso-privacidad` sin enlaces decorativos.
- `Header` implementa identidad con logotipo completo en escritorio e isotipo en vista estrecha; con sesión activa ofrece Inicio, Cotizar, Cotizaciones, Pólizas, Pagos, Ayuda y Perfil.
- `HomePage` implementa la primera versión funcional de la pantalla de inicio.
- `InsuranceCard` reutiliza una misma estructura para seguros de auto y médicos mediante propiedades.
- `NotFoundPage` atiende rutas y pólizas inexistentes; todas las rutas principales tienen pantalla propia.
- `AccessPage` implementa una composición dividida institucional y alterna inicio de sesión y registro mediante pestañas.
- `LoginForm` y `RegisterForm` administran valores y validaciones locales mediante `useState`.
- `FormField` unifica etiquetas, campos y mensajes de error accesibles.
- Registro, login, recuperación de sesión y logout ya consumen Spring Security y persisten clientes en Oracle.
- `DashboardPage` consulta el resumen persistente del cliente, conserva accesos rápidos y limita la actividad reciente a los últimos tres movimientos.
- `DashboardActionCard`, `ActivityList` y `StatusBadge` presentan acciones y estados reutilizables.
- `AccountHistoryPage` presenta el historial completo y separado de cotizaciones, pólizas y pagos con carga, error, reintento, estado vacío, búsqueda y filtro de estado.
- Cada historial se monta con una clave propia para limpiar búsqueda y filtros al cambiar de módulo.
- `QuoteHubPage`, `HelpPage` y `ProfilePage` completan la elección de producto, orientación y consulta de identidad sin inventar operaciones no soportadas.
- `ProfilePage` inicia con campos bloqueados, habilita la edición bajo demanda y activa el guardado después de modificar datos; nombre, apellidos, género, correo y fotografía consumen la API real.
- En escritorio, el perfil utiliza una ficha de dos zonas para identidad/fotografía y formulario; en tablet y móvil vuelve a una columna.
- El correo requiere confirmar la contraseña actual; nacimiento, sexo e identificador fiscal se conservan como datos no editables.
- `panelApi.js` adapta el resumen y el historial real de cotizaciones, pólizas y pagos a los componentes compartidos.
- `FlowSteps` unifica la guía Cotización → Propuesta → Pago y `QuotePageLayout` comparte la estructura institucional de ambos productos.
- `AutoQuoteForm` captura vehículo, licencia, edad, puntos negativos y antecedentes declarados.
- `MedicalQuoteForm` captura edad y cinco factores declarados de salud.
- `SelectField` y `CheckboxField` complementan los controles reutilizables del formulario.
- `quoteApi.js` encapsula la creación y consulta autenticada de cotizaciones mediante la API REST.
- `QuoteResultPage` consulta por identificador y presenta resultados persistidos, aprobados o rechazados, mediante renderizado condicional.
- Una cotización aprobada sin póliza puede continuar a contratación; si ya fue consumida muestra el enlace a su póliza, incluso cuando esta se encuentre cancelada.
- `PaymentPage` integra la contratación con el procesador de pagos controlado por Spring y Oracle.
- `PaymentForm` valida vigencia, modalidad, tarjeta, vencimiento `MM/AA`, CVV y términos; limita caracteres desde escritura y pegado y enfoca el primer error.
- `inputHygiene.js` concentra limpieza alfabética y numérica, identificadores, formato de tarjeta, vencimiento, pegado seguro y enfoque del primer campo inválido.
- El pago permite seleccionar Visa, Mastercard o American Express; React y Spring comprueban que la red, el prefijo, la longitud del PAN y los 3 o 4 dígitos del CVV sean coherentes.
- `paymentApi.js` envía CSRF e idempotencia y evita que los componentes conozcan detalles del transporte.
- Un pago rechazado permanece en la pantalla y no navega hacia una póliza; solamente uno aprobado continúa.
- `PaymentPage` usa la ruta `/cotizaciones/:quoteId/pago`, recarga la cotización real y bloquea formulario, recarga y acceso directo cuando ya existe una póliza o la cotización no fue aprobada.
- `PolicyPage` implementa el detalle de póliza mediante el parámetro dinámico `:polizaId`.
- `PolicyDetails`, `PaymentOverview` y `DocumentList` separan información contractual, pagos y documentos.
- `DocumentList` descarga pólizas y comprobantes PDF reales mediante una ruta protegida.
- Los PDF se regeneran desde datos persistidos. La póliza `VI-POL-2026.2` usa carátula estructurada, logotipo, cláusulas diferenciadas para auto y gastos médicos, estados con color semántico y un único aviso ficticio y educativo.
- Cinzel, Fira Sans y JetBrains Mono se cargan desde recursos del backend y se incrustan en el PDF para conservar la composición en local, contenedores y despliegues remotos.
- El comprobante PDF usa título, autorización, alcance y pie profesionales; póliza y comprobante conservan un solo aviso explícito por documento.
- `CancellationPage` implementa la solicitud y la confirmación sin cancelar inmediatamente la cobertura.
- `CancellationForm` valida motivo, fecha, comentarios y aceptación del proceso.
- `cancellationApi.js` crea solicitudes reales protegidas con CSRF y presenta el folio persistido.
- Al regresar, `PolicyPage` muestra `CANCELACIÓN SOLICITADA` y conserva la indicación de cobertura activa.
- `PolicyPage` oculta la acción de cancelación cuando ya existe una solicitud pendiente.
- La zona `/admin` utiliza contexto, layout, guardia de rutas y servicios independientes del portal del cliente.
- El panel administrativo permite filtrar, consultar, aprobar y rechazar solicitudes con confirmación explícita y observación obligatoria.
- El encabezado administrativo conserva su contexto visual propio y utiliza la misma marca adaptable que el portal del cliente.
- `PolicyPage` presenta `ACTIVA`, `CANCELACION_SOLICITADA` y `CANCELADA` de forma diferenciada; solo una póliza activa ofrece iniciar una cancelación.
- Variables visuales, estilos globales y adaptación inicial para pantallas pequeñas configurados.
- `npm run lint` y `npm run build` terminan correctamente.
- La página de inicio, el acceso, el registro y sus validaciones aprobaron la revisión visual y las pruebas manuales del autor.
- El panel, sus accesos rápidos, la actividad reciente y su adaptación a una columna aprobaron la revisión visual y las pruebas manuales del autor.
- Los formularios de auto y médico, sus validaciones, reglas provisionales y recorridos de aprobación y rechazo aprobaron la revisión visual y las pruebas manuales del autor.
- Las páginas de resultado aprobado y rechazado de ambos seguros aprobaron la revisión visual y las pruebas manuales del autor.
- La contratación, las validaciones y los pagos simulados aprobado y rechazado aprobaron la revisión visual y las pruebas manuales del autor.
- Las pólizas de auto y médica, pagos, documentos PDF reales y diseño adaptable aprobaron la revisión visual y las pruebas manuales del autor.
- La cancelación de ambos tipos de póliza, su estado intermedio y la prevención visual de solicitudes duplicadas aprobaron la revisión visual y las pruebas manuales del autor.

#### Pendientes del frontend

- Crear la etiqueta de entrega después de aprobar las pruebas y la revisión manual.

## Auditoría del recorrido frontend

### Correcciones prioritarias antes del backend

- Corrección terminada: conservar los campos capturados al utilizar “Revisar datos” desde un rechazo.
- Corrección terminada: conservar el tipo de seguro y la cotización al volver desde pago.
- Corrección terminada: rechazar fechas pasadas de vigencia y cancelación, y vencimientos ficticios expirados.
- Corrección terminada: mostrar recurso no encontrado cuando `:polizaId` no exista, sin utilizar una póliza de respaldo.

### Pendientes que dependen de integración

- Corrección terminada: pagos y pólizas utilizan recursos reales asociados al cliente autenticado.
- Corrección terminada: los mocks del recorrido comercial fueron sustituidos por servicios HTTP.
- Corrección terminada: el backend impide accesos a pólizas de otros clientes y operaciones duplicadas.
- Corrección terminada: generar, autorizar y descargar documentos PDF reales.

### Calidad de cierre

- Las reglas compartidas de validación y el shell responsivo cuentan con 8 pruebas automatizadas aprobadas.
- `NotFoundPage` sustituyó a `ComingSoonPage` como página 404 definitiva.
- Encabezado móvil, sidebar, drawer, navegación por teclado y consistencia visual aprobaron la revisión manual final.

### Modelo relacional

- Modelo lógico para NEDW aprobado a partir del diagrama Mermaid revisado por el autor.
- Conserva género, sexo, tarjetas ficticias y tipos de tarjeta.
- Sustituye pago por intento de pago y ubica la emisión de póliza después de un intento aprobado.
- Obtiene la cotización de una póliza por medio del intento de pago, sin una segunda relación directa.
- Incorpora estados e historial de póliza, solicitudes de cancelación y documentos.
- Excluye biometría, siniestros y drogas del alcance NEDW.

#### Resultado del modelo físico para NEDW

- Tipos de ER/Studio traducidos a tipos apropiados de Oracle Database 26ai.
- Columnas identidad, índices y restricciones esenciales implementados.
- Tratamiento del número ficticio de tarjeta diseñado para AES-GCM en Spring, sin persistir CVV.
- Reglas entre tablas implementadas mediante restricciones y triggers.
- DDL, catálogos, datos ficticios y pruebas reproducibles disponibles en `database/`.

### Entorno Oracle

- Oracle Database 26ai está instalado en Oracle Linux dentro de Docker.
- La CDB `FREE` y la PDB `ETOBDA_S1` están abiertas y verificadas.
- El servicio `etobda_s1.fi.unam` es accesible desde macOS mediante `localhost:1522`.

#### Resultado de Oracle para NEDW

- Esquema mínimo desplegado en `VI_APP@ETOBDA_S1`.
- Cinco incrementos ejecutados de forma repetible desde SQL Developer en VS Code.
- Restricciones, triggers, estados y operaciones principales verificados.
- Comandos reproducibles de creación, carga y prueba documentados.

## Alcance descartado para la entrega

- Recuperación y cambio de contraseña.
- Datos fiscales CFDI y domicilio/contacto ampliado.
- Gestión persistente de tarjetas del cliente.
- Facturación fiscal real.
- Registro y seguimiento de siniestros o tickets.

Estos puntos no se consideran pendientes del producto actual. Después del cierre se priorizará el despliegue en OCI y Cloudflare.

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

Crear la etiqueta final `v1.0.0-nedw`. Después, comenzar la preparación del despliegue en OCI y Cloudflare sin ampliar el alcance funcional de la entrega.

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
| 20/11/2026 | Se preparó el incremento de cotizaciones. | Cuatro tablas, especialización protegida, historial automático, cuatro casos académicos y pruebas repetibles quedaron listos. | Ejecutar dos veces estructura y carga, y después las pruebas. |
| 20/11/2026 | Se ejecutó y verificó el incremento de cotizaciones. | Estructura y carga repetibles, cuatro triggers válidos, subtipos exclusivos e historial inicial pasaron todas las pruebas. | Registrar el incremento y continuar con pagos y pólizas. |
| 20/11/2026 | Se preparó el incremento de pagos y pólizas. | Cuatro tablas, reglas concurrentes de aprobación, emisión controlada, historial, documentos y pruebas con rollback quedaron listos. | Ejecutar dos veces la estructura y después las pruebas transaccionales. |
| 20/11/2026 | El primer trigger de pagos devolvió `ORA-25006`. | Oracle no admite el BLOB `comprobante_pdf` en `update of`; el trigger se cambió para ejecutarse ante cualquier actualización. | Repetir el script idempotente y ejecutar las pruebas. |
| 20/11/2026 | Se repitió y verificó el incremento de pagos y pólizas. | Los tres triggers quedaron válidos; aprobación única, emisión, modalidad, costo, historial, documento y rollback pasaron todas las pruebas. | Registrar el incremento y continuar con cancelaciones. |
| 20/11/2026 | Se preparó el incremento de cancelaciones. | La tabla, restricciones, índices, sincronización de estados, exclusión concurrente y pruebas con rollback quedaron listas. | Ejecutar dos veces la estructura y después las pruebas transaccionales. |
| 20/11/2026 | Se ejecutó y verificó el incremento de cancelaciones. | La repetibilidad, fechas, exclusión de duplicados, resoluciones, sincronización con póliza, historial y rollback pasaron correctamente. | Registrar el cierre de Oracle e iniciar la preparación de Spring Boot. |
| 20/11/2026 | Se preparó la prueba E2E transaccional de Oracle. | Se cubren seis recorridos de auto y médico, resultados positivos y negativos, emisión, documentos, cancelaciones y rollback integral. | Ejecutar la prueba de aceptación y revisar cualquier diferencia antes de Spring Boot. |
| 20/11/2026 | Se ejecutó y aprobó la prueba E2E transaccional de Oracle. | Los seis recorridos, ocho rechazos esperados, relaciones, historiales y conteos fueron correctos; las cinco comprobaciones posteriores al rollback devolvieron cero. | Registrar la prueba y preparar la estructura de Spring Boot. |
| 20/11/2026 | Se generó el backend desde Spring Initializr. | Spring Boot 4.2.0, Java 21, Maven, Web MVC, JPA, Validation, Security, Oracle y Actuator compilaron con `BUILD SUCCESS`. | Configurar el datasource sin versionar secretos y probar la conexión real. |
| 20/11/2026 | Se preparó la configuración segura de Oracle. | Variables externas, Hikari, JPA no destructivo, salud sin detalles y denegación temporal de rutas compilaron con `BUILD SUCCESS`. | Probar arranque, salud y contexto con la contraseña proporcionada localmente. |
| 20/11/2026 | Se verificó la conexión Spring Boot–Oracle. | La aplicación inició, `/actuator/health` respondió `200 UP` y `contextLoads()` terminó con una prueba aprobada y `BUILD SUCCESS`. | Implementar la lectura del primer catálogo mediante una ruta vertical. |
| 20/11/2026 | Se preparó el primer recorrido vertical del backend. | El catálogo de géneros separa entidad, Repository, Service Layer, DTO y controlador; seguridad permite solo su lectura y todo compila. | Ejecutar la prueba de integración y revisar la respuesta HTTP real. |
| 20/11/2026 | Se verificó el catálogo público de géneros. | Dos pruebas pasaron; `GET /catalogos/generos` respondió `200` con los cinco registros de Oracle y `POST` fue rechazado con `403`. | Registrar el incremento y preparar registro/autenticación. |
| 20/11/2026 | Se preparó el registro seguro de clientes. | Entidad y repositorio, validaciones, fotografía, BCrypt, errores uniformes, CSRF, sesión explícita y siete pruebas de integración compilan correctamente. | Ejecutar pruebas con Oracle y realizar el recorrido HTTP multipart. |
| 20/11/2026 | Se verificó el registro seguro de clientes. | Nueve pruebas pasaron; el registro respondió `201`, `/auth/me` respondió `200` con sesión y `401` sin cookie, sin exponer datos sensibles. | Registrar el incremento; implementar login, logout y posteriormente la descarga de fotografía. |
| 20/11/2026 | Se consolidó el inventario de pruebas en `docs/PRUEBAS.md`. | Oracle, Spring Boot, API REST y React cuentan con nombres, tipos, alcances y resultados confirmados en un solo documento. | Mantener el registro en cada incremento e implementar login y logout. |
| 20/11/2026 | Se preparó el incremento de login y logout. | Adaptador de usuarios, autenticación BCrypt, sesión, errores genéricos, logout estándar y siete pruebas nuevas compilan con `BUILD SUCCESS`. | Ejecutar las dieciséis pruebas contra Oracle y verificar el recorrido HTTP manual. |
| 20/11/2026 | Se ejecutaron las pruebas de login y logout. | Después de corregir la normalización previa a `@Email`, las 16 pruebas terminaron sin fallos, errores ni omisiones. | Verificar login, sesión y logout mediante solicitudes HTTP reales. |
| 20/11/2026 | Se verificó manualmente el recorrido HTTP de autenticación. | Login y consulta de sesión respondieron `200`, logout respondió `204` y la sesión invalidada recibió `401`; `JSESSIONID` quedó expirada. | Registrar el incremento e integrar la autenticación real en React. |
| 20/11/2026 | Se preparó la integración de autenticación en React. | Cliente HTTP con CSRF y cookies, contexto de sesión, login real, panel protegido, encabezado y logout compilan; `lint` y `build` pasan. | Ejecutar las pruebas manuales en el navegador antes de registrar el incremento. |
| 20/11/2026 | Se verificó la autenticación real desde React. | Redirección, rechazo de credenciales, login, saludo, persistencia tras recarga, acceso autenticado, logout, protección posterior y diseño adaptable pasaron correctamente. | Registrar el incremento y conectar el formulario ampliado de registro. |
| 20/11/2026 | Se preparó el cambio de fotografía obligatoria a opcional. | DDL idempotente, entidad nullable, multipart opcional, respuesta condicional, contrato y una prueba nueva compilan correctamente. | Ejecutar el DDL y su prueba en Oracle antes de correr las 17 pruebas de Spring. |
| 20/11/2026 | Se verificó la fotografía opcional en Oracle y Spring. | La migración y su repetición pasaron, la fotografía académica persistió y las 17 pruebas de Spring terminaron correctamente. | Registrar el incremento y construir el formulario ampliado de React. |
| 20/11/2026 | Se preparó el registro real en React. | Cliente HTTP compartido, catálogo de géneros, multipart, campos completos, fotografía opcional, validaciones y sesión automática pasan `lint` y `build`. | Ejecutar el recorrido manual antes de registrar el incremento. |
| 20/11/2026 | Se verificó el registro real desde React. | Catálogo, validaciones, alta sin fotografía, datos nullable, sesión automática, recarga, nuevo login, duplicados y diseño adaptable pasaron correctamente. | Registrar el incremento y continuar con el siguiente recorrido comercial real. |
| 20/11/2026 | Se documentaron los patrones de diseño y arquitectura. | Patrones implementados, planificados, ubicaciones, límites y criterios quedaron diferenciados sin atribuir mecanismos internos del framework como implementaciones propias. | Mantener el inventario al implementar Strategy o transiciones de estado. |
| 20/11/2026 | Se preparó la numeración concurrente de cotizaciones. | El DDL crea de forma idempotente una secuencia de cuatro dígitos separada de los seeds y la prueba valida configuración y `NEXTVAL`. | Ejecutar dos veces el esquema de cotizaciones y después su prueba. |
| 20/11/2026 | Se verificó la numeración de cotizaciones en Oracle. | La secuencia se creó, la repetición la conservó y `NEXTVAL` quedó operativo sin alterar seeds, subtipos, historiales ni triggers. | Registrar el incremento e implementar el backend de cotizaciones. |
| 20/11/2026 | Se preparó el backend de cotizaciones. | Entidades, repositorios, DTO, estrategias, Service Layer, propiedad, endpoints y nueve pruebas compilan con `BUILD SUCCESS`. | Ejecutar las 26 pruebas contra Oracle antes de aprobar Strategy. |
| 20/11/2026 | Se ejecutaron las pruebas del backend de cotizaciones. | Las 26 pruebas pasaron; ambos Strategy, subtipos, historial, secuencia, validación, sesión y propiedad funcionan contra Oracle. | Realizar una comprobación HTTP manual antes del commit. |
| 20/11/2026 | Se verificó manualmente la API de cotizaciones. | Auto aprobado y médica rechazada respondieron `201`, la consulta propia `200` y la consulta sin sesión `401`, con datos persistidos coherentes. | Registrar el incremento e integrar las cotizaciones en React. |
| 20/11/2026 | Se preparó la integración de cotizaciones en React. | Formularios reales, CSRF compartido, resultado por ID, recarga, revisión de datos, rutas protegidas y enlace a pago pasan `lint` y `build`. | Ejecutar los recorridos manuales de auto y médico antes del commit. |
| 20/11/2026 | Se verificó la integración de cotizaciones en React. | Ambos productos, decisiones, recarga, revisión de datos, regreso a pago, validaciones, diseño adaptable y resultado inexistente funcionan; se separaron las rutas SPA y API para evitar que el proxy intercepte `/cotizaciones/:id`. | Registrar el incremento e integrar pago y emisión de póliza. |
| 20/11/2026 | Se preparó la integración real de pagos. | Pasarela académica con seis instrumentos, saldos, bloqueo, idempotencia, AES-GCM, intentos, emisión transaccional, póliza, documentos, diez pruebas Spring y frontend real compilan correctamente. | Ejecutar DDL, seed y prueba Oracle; después ejecutar las 36 pruebas Spring. |
| 20/11/2026 | Se verificó la pasarela en Oracle y Spring. | El DDL, los seis instrumentos y la prueba Oracle fueron correctos; las 36 pruebas Spring terminaron sin fallos, errores ni omisiones. | Ejecutar los recorridos manuales de pago desde React. |
| 20/11/2026 | Se verificó el pago real desde React. | Aprobación, fondos insuficientes, bloqueo, rechazo del procesador, CVV incorrecto, instrumento desconocido, saldo exacto, doble envío y validaciones locales pasaron correctamente. | Registrar el incremento e integrar la consulta persistente de pólizas. |
| 20/11/2026 | Se preparó la consulta persistente de pólizas. | DTO anidado, proyección de lectura, autorización por propietario, endpoint, cinco pruebas y página React con carga, error y reintento pasan compilación, `lint` y `build`. | Ejecutar las 41 pruebas con las credenciales locales y realizar el recorrido manual. |
| 20/11/2026 | Se verificó el backend de consulta de pólizas. | Las cinco pruebas nuevas y la suite completa de 41 terminaron sin fallos, errores ni omisiones; la prueba de saldo exacto quedó aislada del uso manual previo. | Verificar recarga, acceso directo y errores desde React. |
| 20/11/2026 | Se verificó la consulta persistente desde React. | Pólizas recién emitidas, recarga, acceso directo, producto médico, identificador inexistente, recuperación de sesión y metadatos de documentos pasaron correctamente. | Registrar el incremento e integrar la descarga protegida de documentos. |
| 20/11/2026 | Se preparó la descarga protegida de documentos. | PDFBox, plantilla contractual versionable, regeneración de BLOB, autorización compuesta, encabezados seguros, descarga React y cinco pruebas nuevas pasan compilación, `lint` y `build`. | Ejecutar las 46 pruebas y revisar manualmente ambos PDFs. |
| 20/11/2026 | Se verificó la descarga protegida de documentos. | Las 46 pruebas pasaron y React descargó póliza y comprobante reales; se corrigieron placas faltantes y modalidades técnicas para mostrarlas en español. | Registrar el incremento e integrar cancelaciones reales. |
| 20/11/2026 | Se implementó y verificó la API de solicitudes de cancelación. | El endpoint valida propiedad, estado, fecha, motivo, confirmación, CSRF y duplicados; crea la solicitud de forma transaccional y las 54 pruebas de Spring terminaron sin fallos, errores ni omisiones. | Sustituir los mocks de cancelación en React y verificar el recorrido completo. |
| 21/11/2026 | Se verificó la integración real de cancelaciones en React. | `lint`, `build`, el recorrido desde el navegador y la persistencia en Oracle fueron correctos; el folio y el estado sobreviven a la recarga y se evita otra solicitud pendiente. | Mejorar la presentación del estado y folio de cancelación en el PDF de póliza. |
| 21/11/2026 | Se localizó la cancelación en el PDF de póliza. | Las 55 pruebas pasaron; una póliza activa muestra un estado legible sin folio y una póliza en revisión muestra `Cancelacion solicitada` junto con su folio real. | Integrar la actividad reciente del panel con datos persistidos. |
| 21/11/2026 | Se implementó y verificó la API real del panel. | `GET /panel` limita la actividad al cliente autenticado, combina cotizaciones y pólizas, refleja cancelaciones y ordena los últimos 20 movimientos; las 61 pruebas pasaron. | Conectar `DashboardPage` y retirar su actividad simulada. |
| 21/11/2026 | Se verificó la integración real del panel en React. | `lint`, `build`, actividad persistente, navegación, recarga, cancelación pendiente, estado vacío y diseño adaptable fueron correctos; `dashboardData.js` fue eliminado. | Preparar el recorrido de demostración y estabilización final. |
| 21/11/2026 | Se fijó la fecha efectiva de cancelación al día actual. | Las 62 pruebas, la interfaz y Oracle fueron correctos; React impide modificar la fecha y Spring rechaza fechas pasadas o futuras. | Crear el modelo administrativo para resolver solicitudes. |
| 21/11/2026 | Se desplegó y verificó el modelo administrativo de cancelaciones. | Roles, usuarios internos, relación de permisos, responsable de resolución, trigger reforzado, seed repetible y pruebas `05`, `07` y `99` terminaron correctamente. | Implementar autenticación administrativa en Spring Security. |
| 21/11/2026 | Se implementó y verificó la autenticación administrativa. | Proveedores separados, roles internos, sesión, CSRF, logout y aislamiento entre rutas de clientes y administradores quedaron aprobados en la suite completa de 71 pruebas. | Implementar la API administrativa de consulta y resolución de cancelaciones. |
| 21/11/2026 | Se implementó y verificó la API administrativa de cancelaciones. | Listado, filtros, detalle, aprobación, rechazo, bloqueo transaccional, responsable, cambio de póliza e historial quedaron aprobados en la suite completa de 83 pruebas. | Construir el panel administrativo en React. |
| 21/11/2026 | Se implementó y verificó el panel administrativo en React. | Acceso interno, sesión aislada, filtros, detalle, aprobación, rechazo, recarga, logout y diseño adaptable funcionan; la póliza cancelada se muestra en rojo, sin cobertura ni nueva acción de cancelación. | Completar la integración final del resultado administrativo en las vistas del cliente y documentos. |
| 21/11/2026 | Se completó y verificó el ciclo administrativo de cancelaciones. | Las 85 pruebas, `lint`, `build` y los recorridos manuales aprobaron; la solicitud usa labels legibles y aprobación o rechazo aparecen en póliza, historial visible, panel y PDF regenerado. | Ejecutar estabilización final y preparar la demostración E2E. |
| 21/11/2026 | Se completó y verificó la navegación de la cuenta del cliente. | Las 93 pruebas y los recorridos manuales aprobaron; Inicio muestra tres movimientos y Cotizaciones, Pólizas y Pagos conservan el historial completo, junto con Cotizar, Ayuda y Perfil. | Continuar con la lógica detallada de los nuevos paneles o la estabilización final. |
| 21/11/2026 | Se implementó y verificó la edición protegida del perfil. | Las 100 pruebas y la revisión manual aprobaron; datos personales, correo y fotografía se actualizan, mientras nacimiento, sexo e identificador fiscal permanecen inmutables. | Continuar con los siguientes paneles o la estabilización final. |
| 23/11/2026 | Se implementó la arquitectura visual institucional del portal. | Acceso dividido, shell responsivo, sidebar, drawer accesible, historiales filtrables, validaciones estrictas, cotización/pago rediseñados y perfil reorganizado pasan análisis estático. | Ejecutar la validación manual integral del nuevo portal. |
| 23/11/2026 | Se amplió el pie de página global y la información legal. | Footer de tres columnas, cintillo regulatorio y rutas de Condiciones Generales y Aviso de Privacidad quedaron navegables. | Revisar textos legales finales antes de una publicación. |
| 23/11/2026 | Se corrigió el estado compartido de los historiales. | Cotizaciones, pólizas y pagos reinician sus filtros al cambiar de módulo. | Confirmar la regresión manual. |
| 24/11/2026 | Se completó la estabilización de la entrega. | Frontend 8/8, backend 100/100, Oracle E2E, `lint`, `build` y revisión manual quedaron aprobados; el commit `2658488` registra el cierre. | Crear la etiqueta final `v1.0.0-nedw`. |
| 24/11/2026 | Se registró la autoría institucional del proyecto. | Footer, README y metadatos de frontend y backend identifican a Emmanuel Trejo Olvera; el commit `db36a00` conserva el cambio. | Ninguno para la entrega funcional. |

