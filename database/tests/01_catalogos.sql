-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar estructura, restricciones y datos de catalogos.
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
            raise_application_error(-20020, p_mensaje);
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

    select count(*)
      into v_actual
      from user_tables
     where table_name in (
        'GENERO',
        'TIPO_TARJETA',
        'MODALIDAD_PAGO',
        'ESTATUS_COTIZACION',
        'ESTATUS_INTENTO_PAGO',
        'ESTATUS_POLIZA',
        'ESTATUS_CANCELACION'
     );
    verificar(v_actual = 7, 'se esperaban 7 tablas de catalogo y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_tab_columns
     where table_name in (
        'GENERO',
        'TIPO_TARJETA',
        'MODALIDAD_PAGO',
        'ESTATUS_COTIZACION',
        'ESTATUS_INTENTO_PAGO',
        'ESTATUS_POLIZA',
        'ESTATUS_CANCELACION'
     );
    verificar(v_actual = 21, 'se esperaban 21 columnas de catalogo y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_tab_identity_cols
     where table_name in (
        'GENERO',
        'TIPO_TARJETA',
        'MODALIDAD_PAGO',
        'ESTATUS_COTIZACION',
        'ESTATUS_INTENTO_PAGO',
        'ESTATUS_POLIZA',
        'ESTATUS_CANCELACION'
     );
    verificar(v_actual = 7, 'se esperaban 7 columnas identity y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'P'
       and table_name in (
        'GENERO',
        'TIPO_TARJETA',
        'MODALIDAD_PAGO',
        'ESTATUS_COTIZACION',
        'ESTATUS_INTENTO_PAGO',
        'ESTATUS_POLIZA',
        'ESTATUS_CANCELACION'
       );
    verificar(v_actual = 7, 'se esperaban 7 llaves primarias y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'U'
       and table_name in (
        'GENERO',
        'TIPO_TARJETA',
        'MODALIDAD_PAGO',
        'ESTATUS_COTIZACION',
        'ESTATUS_INTENTO_PAGO',
        'ESTATUS_POLIZA',
        'ESTATUS_CANCELACION'
       );
    verificar(v_actual = 7, 'se esperaban 7 restricciones unique y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'C'
       and constraint_name = 'CK_MODALIDAD_PAGO_INTERVALO'
       and table_name = 'MODALIDAD_PAGO'
       and status = 'ENABLED';
    verificar(v_actual = 1, 'falta la restriccion ck_modalidad_pago_intervalo habilitada');

    select count(*) into v_actual from genero;
    verificar(v_actual = 5, 'genero debe contener 5 registros y contiene ' || v_actual);

    select count(*) into v_actual from tipo_tarjeta;
    verificar(v_actual = 2, 'tipo_tarjeta debe contener 2 registros y contiene ' || v_actual);

    select count(*) into v_actual from modalidad_pago;
    verificar(v_actual = 2, 'modalidad_pago debe contener 2 registros y contiene ' || v_actual);

    select count(*) into v_actual from estatus_cotizacion;
    verificar(v_actual = 5, 'estatus_cotizacion debe contener 5 registros y contiene ' || v_actual);

    select count(*) into v_actual from estatus_intento_pago;
    verificar(v_actual = 3, 'estatus_intento_pago debe contener 3 registros y contiene ' || v_actual);

    select count(*) into v_actual from estatus_poliza;
    verificar(v_actual = 4, 'estatus_poliza debe contener 4 registros y contiene ' || v_actual);

    select count(*) into v_actual from estatus_cancelacion;
    verificar(v_actual = 3, 'estatus_cancelacion debe contener 3 registros y contiene ' || v_actual);

    dbms_output.put_line('pruebas de catalogos completadas correctamente.');
end;
/

select 'genero' catalogo, count(*) registros from genero
union all
select 'tipo_tarjeta', count(*) from tipo_tarjeta
union all
select 'modalidad_pago', count(*) from modalidad_pago
union all
select 'estatus_cotizacion', count(*) from estatus_cotizacion
union all
select 'estatus_intento_pago', count(*) from estatus_intento_pago
union all
select 'estatus_poliza', count(*) from estatus_poliza
union all
select 'estatus_cancelacion', count(*) from estatus_cancelacion
order by catalogo;

