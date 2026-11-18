# Estado del proyecto — Virtual Insurance

**Última actualización:** 17 de noviembre de 2026  
**Entrega de NEDW:** 26 de noviembre de 2026  
**Responsable:** Emmanuel Trejo Olvera

## Resumen ejecutivo

- El alcance prioritario de NEDW está definido.
- El anteproyecto y el prototipo navegable existen.
- Existe una primera versión del modelo relacional, todavía en revisión lógica.
- El entorno Oracle Database 26ai está disponible en Docker.
- El frontend React está en desarrollo; el backend Spring Boot y la integración con Oracle todavía no han comenzado.

## Estado por área

| Área | Estado | Observaciones |
|---|---|---|
| Anteproyecto | Terminado | Documento base disponible en `anteproyecto/`. |
| Prototipo Figma | Terminado con revisión pendiente | Diez pantallas y recorridos principales navegables. |
| Modelo relacional | Parcial | Primera versión modular; faltan ajustes y reglas para el recorrido web. |
| Oracle | Entorno disponible | Software funcional; falta desplegar el esquema definitivo del proyecto. |
| Frontend React | En desarrollo | Base Vite sustituida por rutas, layout compartido, página de inicio y marcadores de pantallas pendientes. |
| Backend Spring Boot | No iniciado | Se implementará después de estabilizar contratos y modelo mínimo. |
| Spring Security | No iniciado | Falta decidir y documentar el mecanismo concreto de autenticación. |
| Integración completa | No iniciada | Depende del frontend, API y esquema Oracle. |
| Pruebas | No iniciadas | Se definirán junto con cada módulo y el recorrido integral. |
| Repositorio Git | Configurado | Rama `main` sincronizada con `origin/main` en GitHub. |

## Componentes disponibles

### Documentación

- `README.md`: presentación general del repositorio.
- `RESUMEN-ASPECTOS-GENERALES.md`: diagnóstico previo del proyecto.
- `docs/ROADMAP.md`: fases, entregables y fechas objetivo.
- `docs/ESTADO.md`: registro vivo del avance.
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
- `ComingSoonPage` identifica visualmente las rutas todavía pendientes.
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
- Variables visuales, estilos globales y adaptación inicial para pantallas pequeñas configurados.
- `npm run lint` y `npm run build` terminan correctamente.
- La página de inicio, el acceso, el registro y sus validaciones aprobaron la revisión visual y las pruebas manuales del autor.
- El panel, sus accesos rápidos, la actividad reciente y su adaptación a una columna aprobaron la revisión visual y las pruebas manuales del autor.
- Los formularios de auto y médico, sus validaciones, reglas provisionales y recorridos de aprobación y rechazo aprobaron la revisión visual y las pruebas manuales del autor.
- Las páginas de resultado aprobado y rechazado de ambos seguros aprobaron la revisión visual y las pruebas manuales del autor.

#### Pendientes del frontend

- Implementar pago, póliza y cancelación.
- Incorporar estados de carga, validación y error.
- Sustituir posteriormente los datos simulados por servicios HTTP.
- Conectar acceso y registro con Spring Security y eliminar la simulación local.
- Sustituir `quoteRules.js` por estrategias del backend y reglas definitivas documentadas.
- Revisar visualmente la aplicación en el navegador y comparar con Figma.
- Conservar los valores capturados cuando el usuario selecciona “Revisar datos” desde una cotización rechazada; actualmente el formulario se reinicia al montarse nuevamente.

### Modelo relacional

- Incluye clientes, cotizaciones, pólizas, pagos y atención a siniestros.
- Se encuentra separado visualmente en módulos funcionales.
- El discriminante del tipo de siniestro se obtiene por medio de póliza y cotización.

#### Pendientes del modelo

- Cerrar la separación lógica de módulos.
- Traducir tipos de ER/Studio a tipos apropiados de Oracle Database 26ai.
- Definir identidades o secuencias, índices y restricciones `UNIQUE`.
- Implementar reglas que involucran varias tablas.
- Agregar estados e historial mínimo de póliza.
- Modelar intentos y resultados de pago.
- Modelar solicitud y resolución de cancelación.
- Preparar DDL y datos ficticios reproducibles.

### Entorno Oracle

- Oracle Database 26ai está instalado en Oracle Linux dentro de Docker.
- El entorno está preparado para crear la CDB desde cero.

#### Pendientes de Oracle para NEDW

- Crear o confirmar CDB/PDB y usuarios requeridos.
- Desplegar el esquema mínimo de la aplicación.
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

Revisar los cuatro resultados posibles de ambos seguros y sus acciones. Después, explicar y aprobar la pantalla de contratación y pago simulado.

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

