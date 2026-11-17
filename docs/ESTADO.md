# Estado del proyecto — Virtual Insurance

**Última actualización:** 17 de noviembre de 2026  
**Entrega de NEDW:** 26 de noviembre de 2026  
**Responsable:** Emmanuel Trejo Olvera

## Resumen ejecutivo

- El alcance prioritario de NEDW está definido.
- El anteproyecto y el prototipo navegable existen.
- Existe una primera versión del modelo relacional, todavía en revisión lógica.
- El entorno Oracle Database 26ai está disponible en Docker.
- El frontend React, el backend Spring Boot y la integración con Oracle todavía no han comenzado.

## Estado por área

| Área | Estado | Observaciones |
|---|---|---|
| Anteproyecto | Terminado | Documento base disponible en `anteproyecto/`. |
| Prototipo Figma | Terminado con revisión pendiente | Diez pantallas y recorridos principales navegables. |
| Modelo relacional | Parcial | Primera versión modular; faltan ajustes y reglas para el recorrido web. |
| Oracle | Entorno disponible | Software funcional; falta desplegar el esquema definitivo del proyecto. |
| Frontend React | No iniciado | Se desarrollará primero con datos simulados. |
| Backend Spring Boot | No iniciado | Se implementará después de estabilizar contratos y modelo mínimo. |
| Spring Security | No iniciado | Falta decidir y documentar el mecanismo concreto de autenticación. |
| Integración completa | No iniciada | Depende del frontend, API y esquema Oracle. |
| Pruebas | No iniciadas | Se definirán junto con cada módulo y el recorrido integral. |

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

- Revisar textos finales y consistencia visual antes de implementar cada pantalla.
- Definir comportamiento adaptable para pantallas pequeñas.
- Precisar estados de carga, errores de red y sesión expirada.
- Agregar, si se requiere, vistas de historial detallado.

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

### Frontend

- Proyecto React con Vite.
- Rutas y layout general.
- Componentes visuales reutilizables.
- Formularios y validaciones.
- Servicios, adaptadores y mocks.
- Comunicación con la API REST.

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

## Riesgos actuales

- Plazo corto para aprender React e integrar cuatro tecnologías.
- Cambios tardíos en el modelo relacional.
- Intentar cubrir módulos de siniestros que no son esenciales para la demostración comercial.
- Dedicar demasiado tiempo al diseño visual antes de lograr el recorrido funcional.
- Dejar seguridad y pruebas para el final.

## Próxima acción

Comprobar las versiones de Node.js, npm, Java, Maven y Git; después crear el proyecto React con Vite en `frontend/` y explicar su estructura archivo por archivo.

## Bitácora breve

| Fecha | Cambio | Resultado | Pendientes detectados |
|---|---|---|---|
| 17/11/2026 | Se definió la estructura del repositorio y se crearon los documentos de control. | Preparación inicial concluida. | Crear remoto y comenzar React. |

