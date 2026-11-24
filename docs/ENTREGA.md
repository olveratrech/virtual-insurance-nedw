# Lista de cierre y versión de entrega

Esta lista evita etiquetar una versión que no haya sido verificada. Debe completarse en orden y sin omitir resultados fallidos.

## 1. Punto de recuperación

- [x] Tag local previo a estabilización: `checkpoint-pre-entrega-2026-11-23`.
- [x] El tag apunta al commit `1d6a6a5`.
- [x] `.codex/skills/virtual-insurance-ui/SKILL.md` permanece fuera del versionado.

Para regresar al punto de partida sin destruir el trabajo actual, se debe crear una rama desde el tag; no ejecutar `reset --hard`:

```bash
git switch -c recuperacion/pre-entrega checkpoint-pre-entrega-2026-11-23
```

## 2. Verificación automatizada pendiente

- [x] `cd frontend && npm run lint` — 0 errores y 2 advertencias conocidas
- [x] `cd frontend && npm test` — 2 archivos, 8 pruebas aprobadas
- [x] `cd frontend && npm run build` — 139 módulos transformados correctamente
- [x] `cd backend && ./mvnw test` — 100/100 pruebas aprobadas
- [x] Oracle: `database/tests/99_recorrido_completo.sql` — 6 recorridos correctos y rollback sin datos residuales

Registrar los conteos y resultados reales en `docs/PRUEBAS.md`. No marcar un paso como aprobado solo porque el comando inicia correctamente.

## 3. Verificación manual pendiente

- [x] Ejecutar el recorrido principal de `docs/GUIA-DEMO.md`.
- [x] Ejecutar al menos pago rechazado, cotización rechazada y acceso protegido sin sesión.
- [x] Revisar escritorio (`>= 1024px`).
- [x] Revisar tablet y móvil (`< 1024px`).
- [x] Confirmar sidebar, topbar, drawer, Escape, backdrop y cierre por navegación.
- [x] Confirmar filtros independientes en cotizaciones, pólizas y pagos.
- [x] Confirmar acceso, perfil, footer y páginas legales.
- [x] Descargar y abrir póliza y comprobante PDF.

## 4. Commit de estabilización

Agregar solo archivos versionables:

```bash
git add README.md docs frontend
git status --short
git commit -m "test(frontend): preparar estabilizacion y entrega"
```

Antes del commit, `.codex/` debe continuar sin estar en el índice.

## 5. Etiqueta final

La etiqueta propuesta es `v1.0.0-nedw`. Crear únicamente después de aprobar las secciones 2 y 3 y registrar sus resultados:

```bash
git tag -a v1.0.0-nedw -m "Entrega final NEDW 2026"
git show --stat v1.0.0-nedw
```

La publicación del commit y la etiqueta en GitHub es una acción separada:

```bash
git push origin main
git push origin v1.0.0-nedw
```

No publicar antes de confirmar que el repositorio remoto y la rama de destino sean correctos.

## 6. Etapa posterior

Una vez publicada la versión, el siguiente trabajo será exclusivamente de despliegue:

- contenerización reproducible;
- Oracle en OCI;
- backend en infraestructura OCI;
- frontend y DNS mediante Cloudflare;
- secretos, TLS, observabilidad, respaldos y CI/CD;
- conservación del entorno local como alternativa funcional.
