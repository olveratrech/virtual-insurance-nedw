-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar cotizaciones, subtipos, historial y reglas de especializacion.
-- conexion requerida: vi_app@etobda_s1

set serveroutput on

declare
    v_usuario     varchar2(128);
    v_contenedor  varchar2(128);
    v_actual      number;

    procedure verificar(
        p_condicion boolean,
        p_mensaje   varchar2
    ) is
    begin
        if not p_condicion then
            raise_application_error(-20050, p_mensaje);
        end if;
    end verificar;
begin
    v_usuario := sys_context('USERENV', 'CURRENT_USER');
    v_contenedor := sys_context('USERENV', 'CON_NAME');

    verificar(
        v_usuario = 'VI_APP' and v_contenedor = 'ETOBDA_S1',
        'conexion incorrecta. se esperaba VI_APP@ETOBDA_S1 y se obtuvo '
        || v_usuario || '@' || v_contenedor
    );

    select count(*) into v_actual
      from user_tables
     where table_name in (
        'COTIZACION', 'COTIZACION_AUTO', 'COTIZACION_MEDICA',
        'HISTORICO_ESTATUS_COTIZACION'
     );
    verificar(v_actual = 4, 'se esperaban 4 tablas de cotizaciones y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_tab_columns
     where table_name in (
        'COTIZACION', 'COTIZACION_AUTO', 'COTIZACION_MEDICA',
        'HISTORICO_ESTATUS_COTIZACION'
     );
    verificar(v_actual = 31, 'se esperaban 31 columnas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_tab_identity_cols
     where table_name in ('COTIZACION', 'HISTORICO_ESTATUS_COTIZACION');
    verificar(v_actual = 2, 'se esperaban 2 columnas identity y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'P'
       and table_name in (
        'COTIZACION', 'COTIZACION_AUTO', 'COTIZACION_MEDICA',
        'HISTORICO_ESTATUS_COTIZACION'
       );
    verificar(v_actual = 4, 'se esperaban 4 llaves primarias y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'R'
       and table_name in (
        'COTIZACION', 'COTIZACION_AUTO', 'COTIZACION_MEDICA',
        'HISTORICO_ESTATUS_COTIZACION'
       )
       and status = 'ENABLED';
    verificar(v_actual = 6, 'se esperaban 6 llaves foraneas habilitadas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'U'
       and table_name in ('COTIZACION', 'HISTORICO_ESTATUS_COTIZACION');
    verificar(v_actual = 2, 'se esperaban 2 restricciones unique y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_name in (
        'CK_COTIZACION_TIPO',
        'CK_COTIZACION_COSTO',
        'CK_COTIZACION_AUTO_ANIO',
        'CK_COTIZACION_AUTO_CATEGORIA',
        'CK_COTIZACION_AUTO_EDAD',
        'CK_COTIZACION_AUTO_PUNTOS',
        'CK_COTIZACION_AUTO_ANTECEDENTES',
        'CK_COTIZACION_MEDICA_EDAD',
        'CK_COTIZACION_MEDICA_HIPERTENSION',
        'CK_COTIZACION_MEDICA_GLUCOSA',
        'CK_COTIZACION_MEDICA_COLESTEROL',
        'CK_COTIZACION_MEDICA_FUMADOR',
        'CK_COTIZACION_MEDICA_ALCOHOL',
        'CK_HISTORICO_ESTATUS_COT_NUMERO'
     )
       and constraint_type = 'C'
       and status = 'ENABLED';
    verificar(v_actual = 14, 'se esperaban 14 restricciones check y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_indexes
     where index_name in (
        'IX_COTIZACION_CLIENTE',
        'IX_COTIZACION_ESTATUS',
        'IX_HISTORICO_COT_ESTATUS'
     );
    verificar(v_actual = 3, 'se esperaban 3 indices adicionales y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_triggers
     where trigger_name in (
        'TRG_COTIZACION_AUTO_TIPO',
        'TRG_COTIZACION_MEDICA_TIPO',
        'TRG_COTIZACION_CAMBIO_TIPO',
        'TRG_COTIZACION_HISTORIAL'
     )
       and status = 'ENABLED';
    verificar(v_actual = 4, 'se esperaban 4 triggers habilitados y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_objects
     where object_name in (
        'TRG_COTIZACION_AUTO_TIPO',
        'TRG_COTIZACION_MEDICA_TIPO',
        'TRG_COTIZACION_CAMBIO_TIPO',
        'TRG_COTIZACION_HISTORIAL'
     )
       and object_type = 'TRIGGER'
       and status = 'VALID';
    verificar(v_actual = 4, 'los 4 triggers deben estar compilados como validos');

    select count(*) into v_actual
      from cotizacion
     where numero_cotizacion in (2, 3, 4, 5);
    verificar(v_actual = 4, 'se esperaban 4 cotizaciones academicas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from cotizacion_auto ca
      join cotizacion c on c.cotizacion_id = ca.cotizacion_id
     where c.numero_cotizacion in (2, 4);
    verificar(v_actual = 2, 'se esperaban 2 cotizaciones de auto y se encontraron ' || v_actual);

    select count(*) into v_actual
      from cotizacion_medica cm
      join cotizacion c on c.cotizacion_id = cm.cotizacion_id
     where c.numero_cotizacion in (3, 5);
    verificar(v_actual = 2, 'se esperaban 2 cotizaciones medicas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from cotizacion c
     where c.numero_cotizacion in (2, 3, 4, 5)
       and not exists (
            select 1 from cotizacion_auto ca where ca.cotizacion_id = c.cotizacion_id
       )
       and not exists (
            select 1 from cotizacion_medica cm where cm.cotizacion_id = c.cotizacion_id
       );
    verificar(v_actual = 0, 'existen cotizaciones academicas sin subtipo');

    select count(*) into v_actual
      from cotizacion_auto ca
      join cotizacion_medica cm on cm.cotizacion_id = ca.cotizacion_id;
    verificar(v_actual = 0, 'una cotizacion no puede tener ambos subtipos');

    select count(*) into v_actual
      from cotizacion c
     where (c.tipo_seguro = 'A' and exists (
                select 1 from cotizacion_medica cm where cm.cotizacion_id = c.cotizacion_id
            ))
        or (c.tipo_seguro = 'M' and exists (
                select 1 from cotizacion_auto ca where ca.cotizacion_id = c.cotizacion_id
            ));
    verificar(v_actual = 0, 'existe un subtipo incompatible con tipo_seguro');

    select count(*) into v_actual
      from historico_estatus_cotizacion h
      join cotizacion c on c.cotizacion_id = h.cotizacion_id
     where c.numero_cotizacion in (2, 3, 4, 5);
    verificar(v_actual = 4, 'se esperaban 4 registros historicos iniciales y se encontraron ' || v_actual);

    dbms_output.put_line('pruebas de cotizaciones completadas correctamente.');
end;
/

select to_char(c.numero_cotizacion, 'fm0000') numero,
       c.tipo_seguro,
       ec.clave estatus,
       c.costo_calculado,
       count(h.historico_estatus_cotizacion_id) cambios
  from cotizacion c
  join estatus_cotizacion ec
    on ec.estatus_cotizacion_id = c.estatus_cotizacion_id
  left join historico_estatus_cotizacion h
    on h.cotizacion_id = c.cotizacion_id
 where c.numero_cotizacion in (2, 3, 4, 5)
 group by c.numero_cotizacion, c.tipo_seguro, ec.clave, c.costo_calculado
 order by c.numero_cotizacion;

