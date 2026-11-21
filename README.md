# Virtual Insurance

Proyecto final de **Negocios Electrónicos y Desarrollo Web** de la Facultad de Ingeniería, UNAM. Consiste en una aplicación web para cotizar y contratar seguros de auto y médicos, consultar pólizas y pagos, y solicitar cancelaciones.

## Estado actual

- Anteproyecto terminado.
- Prototipo navegable elaborado en Figma.
- Esquema funcional desplegado y probado en Oracle Database 26ai.
- Recorrido E2E transaccional de base de datos aprobado.
- Frontend React integrado con Spring Boot para registro, autenticación, cotizaciones, pagos, emisión, consulta de pólizas y descarga de documentos.
- Backend Spring Boot conectado a Oracle, con 46 pruebas automatizadas aprobadas.
- Pasarela académica con tarjetas exclusivamente ficticias, saldos de prueba e idempotencia.
- Pólizas y comprobantes PDF reales, protegidos por sesión y propiedad del recurso.
- Siguiente incremento: integrar las solicitudes de cancelación de React con Spring Boot y Oracle.

Consulta el [roadmap](docs/ROADMAP.md), el [estado detallado](docs/ESTADO.md), el [registro consolidado de pruebas](docs/PRUEBAS.md) y los [patrones de diseño y arquitectura](docs/PATRONES-DISENO.md).

## Tecnologías propuestas

- React para el frontend.
- Spring Boot y Spring Security para el backend.
- API REST para la comunicación entre frontend y backend.
- Oracle Database 26ai para la persistencia.
- Figma para el mockup y el prototipo navegable.

## Estructura del repositorio

```text
proyecto-final/
├── anteproyecto/  # Documento del anteproyecto
├── backend/       # API REST con Spring Boot y Spring Security
├── database/      # Scripts y documentación de Oracle
├── docs/          # Roadmap, estado y documentación técnica
├── frontend/      # Aplicación React creada con Vite
├── README.md
└── RESUMEN-ASPECTOS-GENERALES.md
```

Cada cambio funcional debe acompañarse de pruebas y de una actualización breve de `docs/ESTADO.md` y `docs/PRUEBAS.md`.

## Ejecución

Consulta las instrucciones particulares en `frontend/`, [backend/README.md](backend/README.md) y [database/README.md](database/README.md).

## Autor

Emmanuel Trejo Olvera  
Facultad de Ingeniería, Universidad Nacional Autónoma de México.
