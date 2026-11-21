# Frontend de Virtual Insurance

Aplicación React creada con Vite para el portal académico de Virtual Insurance.

## Módulos integrados

- Registro, inicio y cierre de sesión con Spring Security.
- Cotizaciones de auto y seguro médico.
- Resultados aprobados y rechazados persistidos.
- Contratación mediante una pasarela académica con tarjetas ficticias.
- Emisión y consulta de pólizas.
- Descarga protegida de póliza y comprobante PDF.

La solicitud de cancelación conserva por ahora su implementación de interfaz y es el siguiente módulo que debe conectarse con Spring Boot y Oracle.

## Ejecución

```bash
npm install
npm run dev
```

Vite publica normalmente la aplicación en `http://localhost:5173` y reenvía las solicitudes bajo `/api` al backend local.

## Verificación

```bash
npm run lint
npm run build
```

El estado funcional, las pruebas manuales y los pendientes se mantienen en `../docs/ESTADO.md`, `../docs/PRUEBAS.md` y `../docs/ROADMAP.md`.
