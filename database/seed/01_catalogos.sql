-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: cargar los valores iniciales de los catalogos.
-- conexion requerida: vi_app@etobda_s1

declare
    v_usuario     varchar2(128);
    v_contenedor  varchar2(128);
begin
    v_usuario := sys_context('USERENV', 'CURRENT_USER');
    v_contenedor := sys_context('USERENV', 'CON_NAME');

    if v_usuario <> 'VI_APP' or v_contenedor <> 'ETOBDA_S1' then
        raise_application_error(
            -20010,
            'conexion incorrecta. se esperaba VI_APP@ETOBDA_S1 y se obtuvo '
            || v_usuario || '@' || v_contenedor
        );
    end if;
end;
/

merge into genero destino
using (
    select 'MUJER' clave, 'Mujer' descripcion from dual union all
    select 'HOMBRE', 'Hombre' from dual union all
    select 'NO_BINARIO', 'No binario' from dual union all
    select 'OTRO', 'Otro' from dual union all
    select 'PREFIERE_NO_DECIR', 'Prefiere no decir' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

merge into tipo_tarjeta destino
using (
    select 'CREDIT' clave, 'Credito' descripcion from dual union all
    select 'DEBIT', 'Debito' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

merge into modalidad_pago destino
using (
    select 'MONTHLY' clave, 1 intervalo_meses from dual union all
    select 'ANNUAL', 12 from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.intervalo_meses = fuente.intervalo_meses
when not matched then
    insert (clave, intervalo_meses)
    values (fuente.clave, fuente.intervalo_meses);

merge into estatus_cotizacion destino
using (
    select 'BORRADOR' clave, 'Cotizacion en captura' descripcion from dual union all
    select 'ENVIADA', 'Cotizacion enviada para evaluacion' from dual union all
    select 'APROBADA', 'Cotizacion aprobada y disponible para contratacion' from dual union all
    select 'RECHAZADA', 'Cotizacion rechazada por las reglas de evaluacion' from dual union all
    select 'EXPIRADA', 'Cotizacion fuera de vigencia' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

merge into estatus_intento_pago destino
using (
    select 'PENDIENTE' clave, 'Intento pendiente de resolucion' descripcion from dual union all
    select 'APROBADO', 'Intento aprobado por el simulador' from dual union all
    select 'RECHAZADO', 'Intento rechazado por el simulador' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

merge into estatus_poliza destino
using (
    select 'ACTIVA' clave, 'Poliza activa con cobertura vigente' descripcion from dual union all
    select 'CANCELACION_SOLICITADA', 'Poliza activa con una cancelacion en revision' from dual union all
    select 'CANCELADA', 'Poliza cancelada' from dual union all
    select 'VENCIDA', 'Poliza cuya vigencia ha terminado' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

merge into estatus_cancelacion destino
using (
    select 'SOLICITADA' clave, 'Solicitud recibida y pendiente de resolucion' descripcion from dual union all
    select 'APROBADA', 'Solicitud de cancelacion aprobada' from dual union all
    select 'RECHAZADA', 'Solicitud de cancelacion rechazada' from dual
) fuente
on (destino.clave = fuente.clave)
when matched then
    update set destino.descripcion = fuente.descripcion
when not matched then
    insert (clave, descripcion)
    values (fuente.clave, fuente.descripcion);

commit;

