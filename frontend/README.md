# Frontend de Virtual Insurance

Aplicación React creada con Vite para el portal académico de Virtual Insurance.

## Módulos integrados

- Registro, inicio y cierre de sesión con Spring Security.
- Cotizaciones de auto y seguro médico.
- Resultados aprobados y rechazados persistidos.
- Contratación mediante una pasarela académica con tarjetas ficticias.
- Emisión y consulta de pólizas.
- Descarga protegida de póliza y comprobante PDF.
- Solicitud y seguimiento de cancelaciones.
- Shell responsivo con sidebar de escritorio y drawer móvil.
- Historiales separados de cotizaciones, pólizas y pagos.
- Perfil editable y footer institucional global.

La recuperación y el cambio de contraseña, CFDI, tarjetas guardadas y siniestros no forman parte del alcance de entrega.

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
npm test
```

El estado funcional, las pruebas manuales y los pendientes se mantienen en `../docs/ESTADO.md`, `../docs/PRUEBAS.md` y `../docs/ROADMAP.md`.
