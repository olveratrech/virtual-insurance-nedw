# Resumen de aspectos generales de Virtual Insurance

> **Documento histórico:** este diagnóstico describe el estado inicial anterior a la integración. Para continuar el desarrollo se deben consultar `docs/ESTADO.md`, `docs/ROADMAP.md` y `docs/PRUEBAS.md`.

## Estado general

- El anteproyecto está elaborado y establece los objetivos, el alcance funcional, la estrategia y el plan de trabajo.
- La integración completa de la aplicación todavía no ha comenzado.
- El primer avance técnico consiste en una primera versión del modelo relacional y su separación preliminar por áreas funcionales.
- Existe un entorno funcional con Oracle Database 26ai sobre Oracle Linux dentro de Docker, preparado para crear la CDB desde cero. No se ha indicado que la CDB, las PDB o los objetos del modelo ya hayan sido creados.

## 1. Aspectos generales del proyecto

### Propósito

- Desarrollar una aplicación web para Virtual Insurance que permita:
  - Registrar y autenticar clientes.
  - Cotizar seguros de auto y seguros médicos.
  - Determinar si una cotización es aprobada o rechazada mediante reglas académicas documentadas.
  - Mostrar el costo y la vigencia de una cotización aprobada.
  - Contratar un seguro y registrar el resultado de un pago simulado.
  - Emitir y consultar pólizas.
  - Consultar cotizaciones, pagos y documentos.
  - Solicitar y consultar la cancelación de una póliza.
- Integrar el recorrido comercial con una base de datos Oracle, conservando la consistencia y el historial de la información.

### Alcance previsto

#### Incluido en la primera versión

- Portal web en español para clientes individuales.
- Presentación de seguros de auto y médicos.
- Registro, inicio de sesión y panel del cliente.
- Formularios diferenciados para ambos tipos de cotización.
- Cotizaciones aprobadas y rechazadas.
- Precios personalizados calculados con reglas académicas.
- Contratación individual de diferentes seguros.
- Propuesta y selección de fechas para el inicio de la cobertura.
- Solicitud de cita de asesoría con una fecha propuesta por el sistema y una alternativa del cliente.
- Simulación de pagos aprobados o rechazados, sin efectuar cargos reales.
- Generación y consulta de pólizas y comprobantes de demostración.
- Solicitud y seguimiento de cancelaciones.
- Pruebas de los recorridos exitosos y de los principales casos de error.

#### Fuera del alcance inicial

- Carrito de compra; cada seguro se contratará mediante una operación independiente.
- Aplicación móvil nativa.
- Captura biométrica mediante dispositivos.
- Seguimiento geográfico en tiempo real.
- Asignación automática de ajustadores.
- Atención integral de siniestros desde la primera versión web.
- Renovaciones ejecutadas automáticamente.
- Cargos recurrentes o pagos reales.
- Facturación fiscal certificada.
- Devoluciones monetarias reales.

### Módulos y componentes

#### Áreas funcionales representadas en el primer modelo

- **Clientes:** datos personales, género y datos biométricos.
- **Cotización:** cotización general, cotizaciones de auto y médicas, estados, historial de estados, drogas y antecedentes de siniestros automovilísticos.
- **Pólizas y pagos:** pólizas, renovaciones, modalidades de pago, tarjetas, tipos de tarjeta y pagos.
- **Atención a clientes:** siniestros médicos y de auto, hospitales, ajustadores e historial de ubicaciones.

#### Agrupación modular propuesta

- **Módulo comercial:** clientes, cotizaciones, pólizas y pagos. Es el módulo principal para la primera versión web.
- **Módulo de atención:** siniestros, hospitales, ajustadores y ubicaciones.
- La división definitiva de tablas entre módulos y PDB aún está en revisión; no debe considerarse implementada.

### Fases previstas

1. Definición de reglas, estados, entradas, resultados y alcance.
2. Diseño del mockup y prototipo navegable.
3. Revisión del modelo lógico y preparación de la persistencia.
4. Construcción del backend y de la API REST.
5. Construcción del frontend.
6. Integración del recorrido comercial.
7. Pruebas, correcciones y preparación de la demostración.

### Tecnologías y recursos clave

- **Frontend:** React.
- **Backend:** Spring Boot.
- **Seguridad:** Spring Security.
- **Integración:** API REST.
- **Persistencia:** Oracle Database 26ai.
- **Entorno de base de datos:** Oracle Linux dentro de contenedores Docker.
- **Modelado relacional:** ER/Studio.
- **Mockup y prototipo:** Figma.
- **Control de versiones:** Git; plataforma remota no especificada en el anteproyecto.
- **Datos de demostración:** datos ficticios.
- Las bibliotecas concretas de React, el mecanismo de acceso a datos desde Spring, el formato de autenticación y la herramienta de pruebas no están especificados en el anteproyecto.

## 2. Lo que ya se tiene integrado: primer avance

### Elementos disponibles

- **Anteproyecto terminado:** contiene objetivos, alcance, estrategia, tecnologías propuestas y plan inicial.
- **Entorno Oracle disponible:** Oracle Database 26ai está instalado y funcional dentro de Oracle Linux y Docker para crear la CDB desde cero.
- **Primera versión del modelo relacional:** representa una parte amplia del caso de estudio y contempla:
  - Clientes, género, fotografía y huellas.
  - Cotizaciones, tipos de seguro y estados con historial.
  - Datos especializados para seguros de auto y médicos.
  - Antecedentes de siniestros automovilísticos y drogas asociadas a una cotización médica.
  - Pólizas, renovaciones, modalidades de pago, tarjetas y pagos.
  - Siniestros médicos y automovilísticos, hospitales, ajustadores e historial de ubicaciones.
- **Separación funcional preliminar:** el diagrama distingue visualmente clientes, cotizaciones, pólizas y atención a clientes.

### Detalles faltantes dentro del primer avance

#### Revisión y cierre del modelo lógico

- Falta concluir la revisión y aprobación del modelo relacional y de la separación de módulos requerida por BDA.
- Falta definir la distribución definitiva en los dos módulos/PDB propuestos y reducir las dependencias que crucen sus límites.
- Las cuatro áreas coloreadas del diagrama representan agrupaciones funcionales preliminares; no equivalen todavía a cuatro PDB implementadas.
- Debe corregirse la nulabilidad del apellido paterno, pues el caso permite que no sea especificado.
- El discriminante de los subtipos de siniestro se obtendrá mediante la ruta `SINIESTRO -> POLIZA -> COTIZACION.TIPO_SEGURO`. Falta comprobar en la implementación que cada siniestro pertenezca exclusivamente al subtipo correspondiente.
- Falta asegurar que las especializaciones de cotización y siniestro sean completas y exclusivas: cada registro debe pertenecer a exactamente uno de sus subtipos.

#### Restricciones pendientes de expresar o implementar

- ER/Studio no permitió representar todas las restricciones `UNIQUE`; deben documentarse y agregarse al DDL. Entre ellas se deben revisar:
  - Unicidad de `(CLIENTE_ID, NUMERO_COTIZACION)`.
  - Una sola tarjeta registrada por cliente, si se conserva esa regla del caso.
  - Unicidad de `(CLIENTE_ID, DEDO)` para evitar huellas duplicadas.
  - Unicidad de `(POLIZA_ID, NUMERO_PAGO)`.
  - Unicidad de folios y claves de catálogos.
  - Unicidad de `POLIZA_ANTERIOR_ID` si cada póliza solo puede tener una sucesora.
- Falta definir e implementar las reglas que abarcan varias tablas, por ejemplo:
  - Contratar únicamente cotizaciones aprobadas y vigentes.
  - No activar una póliza cuando el pago sea rechazado.
  - Verificar que la tarjeta corresponda al cliente de la póliza.
  - Conservar sincronizados el estado actual de la cotización y su historial.
  - Evitar ciclos o autorreferencias inválidas en renovaciones.
  - Atender un siniestro solo cuando la póliza esté vigente y corresponda al mismo tipo de seguro.

#### Diseño físico e implementación pendientes

- Los tipos mostrados actualmente por ER/Studio son aproximaciones de una versión anterior y todavía deben traducirse a tipos apropiados de Oracle Database 26ai.
- La selección final de `DATE`, `TIMESTAMP`, `BLOB`, `CLOB`, `JSON` o `XMLTYPE`, según cada atributo, aún no está implementada.
- Las PK consecutivas, secuencias o columnas de identidad, índices, tablespaces y asignaciones de almacenamiento pertenecen al diseño físico y continúan pendientes.
- No se han generado o validado los scripts DDL definitivos.
- No se ha realizado la carga de datos de prueba ni se han comprobado las restricciones con datos reales.
- No se especifica que existan pruebas automatizadas o manuales sobre el modelo.

#### Elementos web aún no incorporados al modelo

- Estados y transiciones de una póliza.
- Historial de cambios de estado de la póliza, si se decide conservarlo.
- Solicitud, resolución y fecha efectiva de cancelación.
- Diferenciación entre intentos de pago aprobados, rechazados o pendientes.
- Registro de la fecha solicitada por el cliente y de la fecha aceptada por el sistema.
- Entidad o estructura para las citas de asesoría.
- Versión o identificación de las reglas de cálculo empleadas en cada cotización, si se requiere reproducir el resultado.
- Las reglas detalladas para estos elementos no están especificadas en los requerimientos y deben definirse como decisiones del prototipo.

### Integraciones que no deben considerarse terminadas

- No existe evidencia de una CDB o PDB creada para este proyecto.
- No existe evidencia de tablas desplegadas en Oracle.
- No existe evidencia de un backend Spring Boot o de configuración de Spring Security.
- No existe evidencia de una API REST funcional.
- No existe evidencia de un frontend React.
- Existe un prototipo navegable en Figma con las diez pantallas del recorrido comercial prioritario; falta trasladarlo a React y revisar los estados de carga y error.
- No existe evidencia de comunicación entre frontend, backend y base de datos.
- No existe evidencia de pagos simulados, emisión de pólizas o cancelaciones funcionales.

## 3. Lo que falta por integrar

### Persistencia y base de datos

- Cerrar y aprobar el modelo lógico y su separación modular.
- Incorporar al modelo los datos mínimos requeridos por los flujos web pendientes.
- Generar el modelo físico para Oracle Database 26ai.
- Crear la CDB, las PDB, los esquemas, usuarios y privilegios requeridos.
- Crear tablas, restricciones, secuencias o identidades, índices y objetos LOB.
- Preparar scripts reproducibles de creación y carga.
- Cargar datos ficticios coherentes para la demostración.
- Probar las restricciones y las operaciones que modifican varias tablas.
- Los tablespaces, almacenamiento, respaldos, FRA, ARCHIVELOG y demás evidencias de BDA pertenecen a una etapa posterior y aún no están integrados.

### Diseño de experiencia e interfaz

- Revisar y ajustar en Figma el mockup de fidelidad media existente.
- Completar en el prototipo los estados de carga, error de red y sesión expirada que resulten necesarios.
- Definir componentes visuales reutilizables.
- Implementar en React las pantallas planificadas.
- Implementar validaciones de formularios, mensajes de error e indicadores de estado.
- Comparar la aplicación funcional con el mockup y documentar las diferencias.

### Backend y seguridad

- Crear el proyecto Spring Boot.
- Definir la estructura de controladores, servicios, repositorios y objetos de transferencia.
- Implementar la conexión con Oracle.
- Implementar la API REST para clientes, cotizaciones, contrataciones, pagos, pólizas, citas y cancelaciones.
- Implementar con Spring Security el registro, la autenticación, la autorización y el aislamiento de datos por cliente.
- Definir el mecanismo concreto de sesión o token; no especificado en el anteproyecto.
- Almacenar contraseñas mediante hashes.
- Controlar operaciones repetidas para evitar pagos o pólizas duplicados.

### Lógica de negocio

- Definir y documentar las reglas académicas de cotización para ambos seguros.
- Implementar cotizaciones aprobadas y rechazadas.
- Generar el documento de rechazo de demostración.
- Implementar la contratación de una cotización aprobada y vigente.
- Implementar las fechas propuestas y aceptadas del servicio.
- Implementar la solicitud de citas de asesoría.
- Implementar el simulador de pagos y sus resultados.
- Emitir y consultar pólizas y comprobantes.
- Implementar la solicitud y seguimiento de cancelaciones.
- La política concreta de cancelación, la fórmula exacta de precios y las condiciones de agenda no están especificadas en los requerimientos.

### Pruebas y entrega

- Preparar escenarios de aceptación para:
  - Registro e inicio de sesión.
  - Cotización aprobada y rechazada.
  - Contratación y pago aprobado.
  - Pago rechazado sin activación de póliza.
  - Consulta posterior de cotizaciones, pagos y pólizas.
  - Solicitud y consulta de cancelación.
  - Accesos no autorizados y entradas inválidas.
- Realizar pruebas de integración entre React, Spring Boot, Spring Security y Oracle.
- Corregir fallos críticos y preparar datos reproducibles para la demostración.
- La herramienta y cobertura concreta de pruebas no están especificadas en el anteproyecto.

### Dependencias críticas

- El diseño físico y los scripts dependen del cierre del modelo lógico y de la división de módulos.
- El backend depende de que las entidades, restricciones y estados necesarios para los flujos web estén definidos.
- El frontend depende de los recorridos validados en Figma y de contratos estables de la API REST.
- La contratación depende de la cotización, de sus estados y de su vigencia.
- La emisión de una póliza depende del resultado aprobado del pago simulado.
- La cancelación depende de que existan estados y transiciones definidos para la póliza.
- Las pruebas integrales dependen de que frontend, backend, seguridad y persistencia puedan ejecutarse conjuntamente.
- La separación entre PDB exige definir cómo validar desde la aplicación las referencias que no puedan conservarse como llaves foráneas entre módulos.

### Riesgos y bloqueos potenciales

- Alcance amplio para un desarrollo individual y un calendario corto.
- Cambios tardíos al modelo que obliguen a modificar el backend y los formularios.
- Acoplamiento frecuente entre clientes, cotizaciones, pólizas y pagos si se separan incorrectamente entre PDB.
- Falta de definición de reglas de precios, cancelación, agenda y transiciones de estado.
- Complejidad de integrar React, Spring Boot, Spring Security y Oracle por primera vez dentro del mismo recorrido.
- Dependencia de la revisión del modelo de BDA antes de consolidar su implementación física.
- Riesgo de dedicar tiempo a funciones fuera del recorrido comercial prioritario, especialmente la gestión completa de siniestros.
- La aceptación del pago simulado como evidencia del requisito de pago debe validarse con el profesor.
- La interpretación de la fecha de entrega del servicio y de la agenda también debe validarse con el profesor.
