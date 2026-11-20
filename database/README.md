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
```

Los scripts comprueban por sí mismos que el usuario y la PDB sean correctos. Si un script de estructura falla parcialmente, revisa primero el error y los objetos alcanzados. Después de corregir la causa puede ejecutarse nuevamente: conservará las tablas existentes y continuará con las faltantes.

## Política de repetibilidad

- Los scripts de creación consultan el diccionario de Oracle antes de crear cada objeto.
- Si una tabla ya existe, se conserva y el script continúa con las siguientes.
- Una tabla existente no se elimina, reemplaza ni modifica silenciosamente.
- Los datos iniciales se cargan con `merge`; volver a ejecutarlos actualiza descripciones y agrega claves faltantes sin duplicarlas.
- Los scripts de pruebas son de solo lectura y pueden ejecutarse cuantas veces sea necesario.
- Las pruebas verifican que los objetos conservados tengan las identidades, PK, claves únicas y datos esperados.
- Un error diferente a la existencia previa de un objeto debe conservarse visible y detener la revisión del incremento.
- Los cambios posteriores usarán comprobaciones específicas antes de cada `alter`.

Esta estrategia permite continuar después de un fallo intermedio sin convertir los scripts en procesos destructivos. No pretende reparar automáticamente una tabla que haya sido alterada manualmente; una diferencia estructural debe detectarse, revisarse y corregirse mediante un cambio explícito.

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
| Número de tarjeta cifrado | `RAW`, tamaño final por confirmar con la implementación AES-GCM |

Las columnas identidad pueden tener saltos por concurrencia, caché o transacciones revertidas. La continuidad visible se resuelve con los folios y números de negocio, no con las PK.

