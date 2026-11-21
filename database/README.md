# Base de datos de Virtual Insurance

Scripts reproducibles para Oracle AI Database 26ai Free, ejecutado en la PDB `ETOBDA_S1`.

## Entorno verificado

```text
CDB: FREE
PDB: ETOBDA_S1
Servicio: etobda_s1.fi.unam
Listener dentro del contenedor: 1521
Puerto publicado en macOS: 1522
Contenedor: c1-bda-eto
Esquema de la aplicación: VI_APP
```

## Estructura

```text
database/
├── README.md
├── admin/       # Operaciones ejecutadas por SYS o un administrador
├── schema/      # Tablas, restricciones, índices y objetos de VI_APP
├── seed/        # Catálogos y datos académicos reproducibles
└── tests/       # Pruebas de estructura, restricciones y recorridos
```

## Orden previsto de ejecución

1. `admin/01_crear_usuario.sql`
2. `admin/02_ajustar_privilegios.sql`, solo para instalaciones creadas antes de incorporar `create sequence`.
3. Scripts de `schema/`, en orden numérico.
4. Scripts de `seed/`, en orden numérico.
5. Scripts de `tests/`, en orden numérico.

No existe un script automático para eliminar el esquema. Cualquier limpieza destructiva se documentará y ejecutará manualmente después de confirmar su objetivo.

## Estilo SQL

- Las palabras reservadas, tablas, columnas, restricciones y variables se escriben en minúsculas.
- Los identificadores no se encierran entre comillas dobles.
- Oracle almacena los identificadores no entrecomillados en mayúsculas dentro del diccionario de datos; esto no cambia la forma de escribir las consultas.
- Los nombres físicos permanecen en español y las claves de catálogo siguen el contrato API.

## Ejecución desde SQL Developer en VS Code

La conexión principal de desarrollo es `VI_APP@ETOBDA_S1`. Antes de cada incremento puede verificarse con:

```sql
show con_name;
show user;
```

Ejecuta los archivos completos mediante la opción **Run Script**, respetando el orden:

```text
schema/01_catalogos.sql
seed/01_catalogos.sql
tests/01_catalogos.sql

schema/02_clientes_tarjetas.sql
seed/02_clientes.sql
tests/02_clientes_tarjetas.sql

schema/03_cotizaciones.sql
seed/03_cotizaciones.sql
tests/03_cotizaciones.sql

schema/04_pagos_polizas.sql
tests/04_pagos_polizas.sql

schema/05_cancelaciones.sql

schema/06_simulador_pagos.sql
seed/04_instrumentos_pago.sql
tests/06_simulador_pagos.sql

schema/07_administracion_cancelaciones.sql
seed/05_usuarios_internos.sql
tests/05_cancelaciones.sql
tests/07_administracion_cancelaciones.sql

tests/99_recorrido_completo.sql
```

Los scripts comprueban por sí mismos que el usuario y la PDB sean correctos. Si un script de estructura falla parcialmente, revisa primero el error y los objetos alcanzados. Después de corregir la causa puede ejecutarse nuevamente: conservará las tablas existentes y continuará con las faltantes.

## Política de repetibilidad

- Los scripts de creación consultan el diccionario de Oracle antes de crear cada objeto.
- Si una tabla ya existe, se conserva y el script continúa con las siguientes.
- Una tabla existente no se elimina, reemplaza ni modifica silenciosamente.
- Los datos iniciales se cargan con `merge`; volver a ejecutarlos actualiza descripciones y agrega claves faltantes sin duplicarlas.
- Los scripts de pruebas pueden ejecutar operaciones transaccionales y terminan con `rollback`; pueden repetirse sin conservar sus datos temporales.
- Las pruebas verifican que los objetos conservados tengan las identidades, PK, claves únicas y datos esperados.
- Un error diferente a la existencia previa de un objeto debe conservarse visible y detener la revisión del incremento.
- Los cambios posteriores usarán comprobaciones específicas antes de cada `alter`.

Esta estrategia permite continuar después de un fallo intermedio sin convertir los scripts en procesos destructivos. No pretende reparar automáticamente una tabla que haya sido alterada manualmente; una diferencia estructural debe detectarse, revisarse y corregirse mediante un cambio explícito.

## Numeración de cotizaciones

`SEQ_NUMERO_COTIZACION` reserva números de negocio entre `1000` y `9999` para las cotizaciones creadas por la aplicación. Las cotizaciones académicas `0002` a `0005` permanecen separadas. La secuencia evita colisiones entre solicitudes concurrentes; sus saltos por caché, reinicios o transacciones revertidas son válidos y no afectan las PK.

## Pasarela académica

`INSTRUMENTO_PAGO_PRUEBA` representa un procesador externo ficticio. Conserva únicamente hash SHA-256 del PAN de prueba, últimos cuatro, saldo, estado y versión de concurrencia. Los saldos no pertenecen a `TARJETA` porque una aseguradora real no administra la cuenta bancaria del cliente.

`SEQ_NUMERO_PAGO` y `SEQ_FOLIO_POLIZA` generan referencias concurrentes. `INTENTO_PAGO.IDEMPOTENCY_KEY` impide registrar dos veces una misma operación. Volver a ejecutar `seed/04_instrumentos_pago.sql` restablece los seis escenarios académicos y sus saldos iniciales.

## Administración de cancelaciones

`USUARIO_INTERNO` mantiene las identidades administrativas separadas de `CLIENTE`. `ROL_INTERNO` y `USUARIO_INTERNO_ROL` asignan las autoridades `ADMIN` y `CANCELLATION_REVIEWER` sin convertir a un cliente en personal de la aseguradora.

`SOLICITUD_CANCELACION.RESUELTA_POR_USUARIO_ID` registra al responsable de una aprobación o rechazo. Una solicitud pendiente conserva este valor en `NULL`; para resolverla, el trigger exige una observación, un usuario activo y uno de los roles autorizados. La aprobación cancela la póliza inmediatamente y el rechazo la devuelve a `ACTIVA`.

La cuenta exclusivamente académica cargada por `seed/05_usuarios_internos.sql` es:

```text
admin.cancelaciones@virtual-insurance.test / Virtual2026!
```

La contraseña se persiste únicamente como hash BCrypt. Estas credenciales no deben reutilizarse fuera del entorno local de demostración.

## Prueba E2E transaccional

`tests/99_recorrido_completo.sql` es la prueba de aceptación integral de Oracle. Utiliza el esquema y los catálogos ya desplegados, crea un cliente temporal y recorre:

- cotizaciones de auto y médicas aprobadas y rechazadas;
- pagos aprobados y rechazados;
- emisión de pólizas y documentos;
- cancelaciones aprobadas y rechazadas;
- errores de negocio críticos y transiciones no permitidas.

El script termina con `rollback` tanto al aprobar como al fallar. Las consultas finales deben devolver cero y confirman que no quedaron datos E2E persistidos.

El inventario de pruebas incrementales, caminos E2E y resultados confirmados se encuentra en el [registro consolidado de pruebas](../docs/PRUEBAS.md).

Después de actualizar los scripts del repositorio, conserva la copia operativa:

```bash
mkdir -p /Users/olveratrech/unam/nedw/scripts
cp -R database/. /Users/olveratrech/unam/nedw/scripts/
```

## Primera ejecución: crear `VI_APP`

El script administrativo:

- cambia la sesión a `ETOBDA_S1`;
- comprueba que el contenedor actual sea el correcto;
- solicita la contraseña sin guardarla en el archivo;
- crea un usuario local;
- asigna `USERS` como tablespace predeterminado y `TEMP` como temporal;
- limita su cuota a 500 MiB;
- concede únicamente los privilegios necesarios para construir el esquema.

Las columnas `identity` requieren que el propietario pueda crear su generador de secuencia interno. Por ello `VI_APP` recibe tanto `create table` como `create sequence`.

### Sincronizar los scripts con el directorio compartido

El repositorio es la fuente de verdad. Desde su raíz en macOS, copia los archivos al directorio compartido con el contenedor:

```bash
mkdir -p /Users/olveratrech/unam/nedw/scripts
cp -R database/. /Users/olveratrech/unam/nedw/scripts/
```

### Ejecutarlo dentro del contenedor

```bash
su - olveratrech
cd "$UNAM_HOME/nedw/scripts"
sqlplus /nolog
```

`/nolog` deja el cliente sin conexión. Antes de ejecutar cualquier script, establece explícitamente una sesión administrativa:

```sql
CONNECT / AS SYSDBA
SHOW USER;
SHOW CON_NAME;
```

El resultado debe mostrar `SYS`. El contenedor inicial puede ser `CDB$ROOT`; el script cambiará a `ETOBDA_S1`.

Si la autenticación del sistema operativo no permite `CONNECT / AS SYSDBA`, utiliza:

```sql
CONNECT SYS@//localhost:1521/etobda_s1.fi.unam AS SYSDBA
```

SQL*Plus solicitará la contraseña. No la escribas dentro del comando ni del archivo.

Solo después de confirmar la conexión ejecuta:

```sql
@admin/01_crear_usuario.sql
```

La contraseña se solicita de forma interactiva. Para evitar problemas con la sustitución de SQL*Plus durante esta preparación, utiliza una contraseña fuerte que no contenga comillas dobles (`"`) ni el carácter `&`.

El script debe terminar mostrando:

```text
ETOBDA_S1
VI_APP  OPEN  USERS
```

### Probar la conexión desde macOS

Si SQL*Plus o SQLcl está instalado en macOS:

```bash
sqlplus VI_APP@//localhost:1522/etobda_s1.fi.unam
```

No escribas la contraseña en el comando. El cliente debe solicitarla.

Si no tienes un cliente Oracle en macOS, la prueba equivalente puede hacerse dentro del contenedor:

```bash
sqlplus VI_APP@//localhost:1521/etobda_s1.fi.unam
```

Dentro de la sesión de `VI_APP`:

```sql
SHOW USER;
SHOW CON_NAME;

SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') AS SERVICIO
FROM DUAL;
```

Resultado esperado:

```text
USER is "VI_APP"
CON_NAME is "ETOBDA_S1"
SERVICIO = etobda_s1.fi.unam
```

## Reglas de seguridad

- No versionar contraseñas, claves de cifrado ni archivos de wallet.
- No ejecutar los scripts de la aplicación en `CDB$ROOT`.
- Spring Boot nunca se conectará como `SYS`, `SYSTEM` o `PDBADMIN`.
- El CVV de una tarjeta no tendrá columna ni se persistirá.
- El número ficticio de tarjeta se cifrará antes de almacenarse.
- Los scripts fallan ante errores SQL para evitar despliegues parcialmente exitosos.
- Los scripts SQL presuponen una conexión activa con los privilegios indicados; `sqlplus /nolog` por sí solo no establece ninguna conexión.

## Convenciones físicas aprobadas

| Uso | Tipo o estrategia |
|---|---|
| Claves primarias | `NUMBER(12)` con `GENERATED BY DEFAULT ON NULL AS IDENTITY` |
| Texto | `VARCHAR2(n CHAR)` |
| Banderas del modelo | `CHAR(1)` con `CHECK` para `S` y `N` |
| Importes | `NUMBER(14,2)` |
| Fecha sin hora | `DATE` |
| Evento con hora | `TIMESTAMP(6) WITH LOCAL TIME ZONE` |
| Fotografía y PDF | `BLOB` |
| Factura JSON | `JSON` |
| Factura XML | `XMLTYPE` |
| Número de tarjeta cifrado | `RAW(128)` con cifrado AES-256-GCM en Spring |

Las columnas identidad pueden tener saltos por concurrencia, caché o transacciones revertidas. La continuidad visible se resuelve con los folios y números de negocio, no con las PK.

## Cifrado del número de tarjeta

- Spring cifrará el número ficticio antes de enviarlo a Oracle.
- El algoritmo previsto es `AES/GCM/NoPadding` con una clave de 256 bits.
- Cada cifrado utilizará un IV aleatorio nuevo; cifrar dos veces el mismo número no producirá el mismo valor.
- El valor binario empaquetará una versión de formato, el IV y el texto cifrado con su etiqueta de autenticación.
- `raw(128)` ofrece espacio suficiente para ese paquete y permite versionar el formato sin cambiar la tabla.
- La clave se proporcionará mediante una variable de entorno y nunca se almacenará en Oracle o Git.
- No se aplica una restricción `unique` al valor cifrado porque el IV aleatorio hace que valores iguales produzcan bytes diferentes.
- La API no devolverá el número completo y el CVV nunca llegará a la base.

## Cliente académico inicial

```text
Correo: emmanuel.demo@virtual-insurance.test
Contraseña: 
```

El script almacena únicamente el hash BCrypt. La cuenta académica conserva una imagen PNG mínima válida, aunque `CLIENTE.FOTO` permite `NULL` para nuevos clientes que no proporcionen fotografía. No se carga una tarjeta desde SQL: debe crearse desde Spring para garantizar que pase por el servicio de cifrado.

