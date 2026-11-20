-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar solicitudes, transiciones, duplicados e historial de poliza.
-- conexion requerida: vi_app@etobda_s1

set serveroutput on

declare
    v_usuario               varchar2(128);
    v_contenedor            varchar2(128);
    v_actual                number;
    v_cliente_id            cliente.cliente_id%type;
    v_tipo_tarjeta_id       tipo_tarjeta.tipo_tarjeta_id%type;
    v_tarjeta_id            tarjeta.tarjeta_id%type;
    v_modalidad_id          modalidad_pago.modalidad_pago_id%type;
    v_intento_aprobado_id   estatus_intento_pago.estatus_intento_pago_id%type;
    v_poliza_activa_id      estatus_poliza.estatus_poliza_id%type;
    v_cancelacion_sol_id    estatus_cancelacion.estatus_cancelacion_id%type;
    v_cancelacion_apr_id    estatus_cancelacion.estatus_cancelacion_id%type;
    v_cancelacion_rech_id   estatus_cancelacion.estatus_cancelacion_id%type;
    v_cotizacion_id         cotizacion.cotizacion_id%type;
    v_intento_pago_id       intento_pago.intento_pago_id%type;
    v_poliza_id             poliza.poliza_id%type;
    v_solicitud_id          solicitud_cancelacion.solicitud_cancelacion_id%type;
    v_clave                 varchar2(30);

    procedure verificar(
        p_condicion boolean,
        p_mensaje   varchar2
    ) is
    begin
        if not p_condicion then
            raise_application_error(-20090, p_mensaje);
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
     where table_name = 'SOLICITUD_CANCELACION';
    verificar(v_actual = 1, 'debe existir la tabla solicitud_cancelacion');

    select count(*) into v_actual
      from user_tab_columns
     where table_name = 'SOLICITUD_CANCELACION';
    verificar(v_actual = 10, 'solicitud_cancelacion debe tener 10 columnas');

    select count(*) into v_actual
      from user_tab_identity_cols
     where table_name = 'SOLICITUD_CANCELACION'
       and column_name = 'SOLICITUD_CANCELACION_ID';
    verificar(v_actual = 1, 'la llave primaria debe ser identity');

    select count(*) into v_actual
      from user_constraints
     where table_name = 'SOLICITUD_CANCELACION'
       and constraint_type = 'P';
    verificar(v_actual = 1, 'se esperaba una llave primaria');

    select count(*) into v_actual
      from user_constraints
     where table_name = 'SOLICITUD_CANCELACION'
       and constraint_type = 'R'
       and status = 'ENABLED';
    verificar(v_actual = 2, 'se esperaban dos llaves foraneas habilitadas');

    select count(*) into v_actual
      from user_constraints
     where constraint_name in (
        'UK_SOLICITUD_CANCELACION_FOLIO',
        'CK_SOLICITUD_CANCELACION_MOTIVO'
     )
       and status = 'ENABLED';
    verificar(v_actual = 2, 'deben estar habilitadas las restricciones de folio y motivo');

    select count(*) into v_actual
      from user_indexes
     where index_name in (
        'IX_SOLICITUD_CANCELACION_POL',
        'IX_SOLICITUD_CANCELACION_EST'
     );
    verificar(v_actual = 2, 'se esperaban dos indices adicionales');

    select count(*) into v_actual
      from user_objects
     where object_name = 'TRG_SOLICITUD_CANCELACION_REG'
       and object_type = 'TRIGGER'
       and status = 'VALID';
    verificar(v_actual = 1, 'el trigger de cancelaciones debe estar valido');

    select cliente_id into v_cliente_id
      from cliente
     where email = 'emmanuel.demo@virtual-insurance.test';

    select tipo_tarjeta_id into v_tipo_tarjeta_id
      from tipo_tarjeta
     where clave = 'CREDIT';

    select modalidad_pago_id into v_modalidad_id
      from modalidad_pago
     where clave = 'MONTHLY';

    select estatus_intento_pago_id into v_intento_aprobado_id
      from estatus_intento_pago
     where clave = 'APROBADO';

    select estatus_poliza_id into v_poliza_activa_id
      from estatus_poliza
     where clave = 'ACTIVA';

    select estatus_cancelacion_id into v_cancelacion_sol_id
      from estatus_cancelacion
     where clave = 'SOLICITADA';

    select estatus_cancelacion_id into v_cancelacion_apr_id
      from estatus_cancelacion
     where clave = 'APROBADA';

    select estatus_cancelacion_id into v_cancelacion_rech_id
      from estatus_cancelacion
     where clave = 'RECHAZADA';

    select cotizacion_id into v_cotizacion_id
      from cotizacion
     where numero_cotizacion = 2;

    insert into tarjeta (
        cliente_id,
        tipo_tarjeta_id,
        numero_tarjeta,
        anio_expiracion,
        mes_expiracion
    ) values (
        v_cliente_id,
        v_tipo_tarjeta_id,
        hextoraw('1112131415161718191A1B1C1D1E1F20'),
        2028,
        12
    ) returning tarjeta_id into v_tarjeta_id;

    insert into intento_pago (
        cotizacion_id,
        tarjeta_id,
        modalidad_pago_id,
        numero_pago,
        fecha_pago,
        importe,
        comprobante_pdf,
        estatus_intento_pago_id,
        motivo_rechazo
    ) values (
        v_cotizacion_id,
        v_tarjeta_id,
        v_modalidad_id,
        951,
        systimestamp,
        1040.00,
        empty_blob(),
        v_intento_aprobado_id,
        null
    ) returning intento_pago_id into v_intento_pago_id;

    insert into poliza (
        intento_pago_id,
        folio,
        vigencia_desde,
        vigencia_hasta,
        costo_seguro,
        iva_importe,
        folio_factura,
        factura_json,
        factura_xml,
        modalidad_pago_id,
        poliza_anterior_id,
        estatus_poliza_id
    ) values (
        v_intento_pago_id,
        'VI-CAN-TEST-001',
        date '2026-11-26',
        date '2027-11-25',
        12480.00,
        1996.80,
        'FAC-CAN-TEST-001',
        json('{"folio":"FAC-CAN-TEST-001"}'),
        xmltype('<factura><folio>FAC-CAN-TEST-001</folio></factura>'),
        v_modalidad_id,
        null,
        v_poliza_activa_id
    ) returning poliza_id into v_poliza_id;

    begin
        insert into solicitud_cancelacion (
            poliza_id, folio, motivo, fecha_solicitada, comentarios,
            estatus_cancelacion_id, fecha_creacion
        ) values (
            v_poliza_id, 'CAN-INVALIDA-FECHA', 'FINANCIAL',
            trunc(current_date) - 1, null, v_cancelacion_sol_id, systimestamp
        );
        raise_application_error(-20091, 'se permitio una fecha solicitada pasada.');
    exception
        when others then
            if sqlcode <> -20083 then
                raise;
            end if;
    end;

    insert into solicitud_cancelacion (
        poliza_id,
        folio,
        motivo,
        fecha_solicitada,
        comentarios,
        estatus_cancelacion_id,
        fecha_creacion
    ) values (
        v_poliza_id,
        'CAN-TEST-001',
        'FINANCIAL',
        trunc(current_date) + 1,
        'solicitud academica de prueba',
        v_cancelacion_sol_id,
        systimestamp
    ) returning solicitud_cancelacion_id into v_solicitud_id;

    select ep.clave into v_clave
      from poliza p
      join estatus_poliza ep
        on ep.estatus_poliza_id = p.estatus_poliza_id
     where p.poliza_id = v_poliza_id;
    verificar(v_clave = 'CANCELACION_SOLICITADA', 'la poliza debe quedar con cancelacion solicitada');

    select count(*) into v_actual
      from historico_estatus_poliza
     where poliza_id = v_poliza_id;
    verificar(v_actual = 2, 'la solicitud debe producir el segundo registro historico');

    begin
        insert into solicitud_cancelacion (
            poliza_id, folio, motivo, fecha_solicitada, comentarios,
            estatus_cancelacion_id, fecha_creacion
        ) values (
            v_poliza_id, 'CAN-TEST-DUP', 'OTHER', trunc(current_date) + 2,
            null, v_cancelacion_sol_id, systimestamp
        );
        raise_application_error(-20092, 'se permitio una segunda solicitud pendiente.');
    exception
        when others then
            if sqlcode not in (-20082, -20088) then
                raise;
            end if;
    end;

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_rech_id,
           observacion_resolucion = 'rechazo academico'
     where solicitud_cancelacion_id = v_solicitud_id;

    select ep.clave into v_clave
      from poliza p
      join estatus_poliza ep
        on ep.estatus_poliza_id = p.estatus_poliza_id
     where p.poliza_id = v_poliza_id;
    verificar(v_clave = 'ACTIVA', 'una cancelacion rechazada debe devolver la poliza a activa');

    select count(*) into v_actual
      from solicitud_cancelacion
     where solicitud_cancelacion_id = v_solicitud_id
       and fecha_resolucion is not null;
    verificar(v_actual = 1, 'la resolucion debe registrar su fecha automaticamente');

    insert into solicitud_cancelacion (
        poliza_id,
        folio,
        motivo,
        fecha_solicitada,
        comentarios,
        estatus_cancelacion_id,
        fecha_creacion
    ) values (
        v_poliza_id,
        'CAN-TEST-002',
        'NOT_NEEDED',
        trunc(current_date) + 3,
        null,
        v_cancelacion_sol_id,
        systimestamp
    ) returning solicitud_cancelacion_id into v_solicitud_id;

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_apr_id,
           observacion_resolucion = 'aprobacion academica'
     where solicitud_cancelacion_id = v_solicitud_id;

    select ep.clave into v_clave
      from poliza p
      join estatus_poliza ep
        on ep.estatus_poliza_id = p.estatus_poliza_id
     where p.poliza_id = v_poliza_id;
    verificar(v_clave = 'CANCELADA', 'una cancelacion aprobada debe cancelar la poliza');

    select count(*) into v_actual
      from historico_estatus_poliza
     where poliza_id = v_poliza_id;
    verificar(v_actual = 5, 'dos solicitudes resueltas deben producir cinco estados de poliza');

    begin
        delete from solicitud_cancelacion
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20093, 'se permitio eliminar una solicitud.');
    exception
        when others then
            if sqlcode <> -20080 then
                raise;
            end if;
    end;

    rollback;
    dbms_output.put_line('pruebas de cancelaciones completadas correctamente.');
exception
    when others then
        rollback;
        raise;
end;
/

select count(*) cancelaciones_prueba_persistidas
  from solicitud_cancelacion
 where folio like 'CAN-TEST-%';

select count(*) polizas_cancelacion_prueba_persistidas
  from poliza
 where folio = 'VI-CAN-TEST-001';
