# Virtual Insurance

Proyecto final de **Negocios Electrónicos y Desarrollo Web** de la Facultad de Ingeniería, UNAM. Consiste en una aplicación web para cotizar y contratar seguros de auto y médicos, consultar pólizas y pagos, y solicitar cancelaciones.

## Estado actual

- Anteproyecto terminado.
- Prototipo navegable elaborado en Figma.
- Esquema funcional desplegado y probado en Oracle Database 26ai.
- Recorrido E2E transaccional de base de datos aprobado.
- Frontend React funcional con datos simulados.
- Backend Spring Boot conectado a Oracle, con catálogo, registro y sesión inicial.

Consulta el [roadmap](docs/ROADMAP.md), el [estado detallado](docs/ESTADO.md) y el [registro consolidado de pruebas](docs/PRUEBAS.md).

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
