-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar identidades internas, roles y resolucion responsable.
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
    v_admin_id              usuario_interno.usuario_interno_id%type;
    v_inactivo_id           usuario_interno.usuario_interno_id%type;
    v_sin_rol_id            usuario_interno.usuario_interno_id%type;
    v_clave                 varchar2(40);
    v_numero_pago           intento_pago.numero_pago%type;

    procedure verificar(p_condicion boolean, p_mensaje varchar2) is
    begin
        if not p_condicion then
            raise_application_error(-20120, p_mensaje);
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
     where table_name in ('ROL_INTERNO', 'USUARIO_INTERNO', 'USUARIO_INTERNO_ROL');
    verificar(v_actual = 3, 'deben existir las tres tablas administrativas');

    select count(*) into v_actual
      from user_tab_columns
     where table_name = 'SOLICITUD_CANCELACION'
       and column_name = 'RESUELTA_POR_USUARIO_ID';
    verificar(v_actual = 1, 'falta el responsable de resolucion en solicitud_cancelacion');

    select count(*) into v_actual
      from user_constraints
     where constraint_name in (
        'PK_ROL_INTERNO', 'UK_ROL_INTERNO_CLAVE',
        'PK_USUARIO_INTERNO', 'UK_USUARIO_INTERNO_EMAIL',
        'PK_USUARIO_INTERNO_ROL', 'FK_USUARIO_INTERNO_ROL_USR',
        'FK_USUARIO_INTERNO_ROL_ROL', 'FK_SOLICITUD_CANCELACION_USR'
     )
       and status = 'ENABLED';
    verificar(v_actual = 8, 'faltan restricciones administrativas habilitadas');

    select count(*) into v_actual
      from user_indexes
     where index_name in ('IX_USUARIO_INTERNO_ROL_ROL', 'IX_SOLICITUD_CANCELACION_USR');
    verificar(v_actual = 2, 'faltan indices administrativos');

    select count(*) into v_actual
      from rol_interno
     where clave in ('ADMIN', 'CANCELLATION_REVIEWER');
    verificar(v_actual = 2, 'faltan roles internos iniciales');

    select usuario_interno_id into v_admin_id
      from usuario_interno
     where email = 'admin.cancelaciones@virtual-insurance.test'
       and activo = 'S'
       and length(contrasena) >= 60;

    select count(*) into v_actual
      from usuario_interno_rol uir
      join rol_interno ri on ri.rol_interno_id = uir.rol_interno_id
     where uir.usuario_interno_id = v_admin_id
       and ri.clave in ('ADMIN', 'CANCELLATION_REVIEWER');
    verificar(v_actual = 2, 'el administrador academico debe conservar ambos roles');

    insert into usuario_interno (
        nombre, email, contrasena, activo, fecha_creacion
    ) values (
        'Revisor inactivo', 'inactivo.admin.test@virtual-insurance.test',
        rpad('$2y$12$test', 60, 'x'), 'N', systimestamp
    ) returning usuario_interno_id into v_inactivo_id;

    insert into usuario_interno (
        nombre, email, contrasena, activo, fecha_creacion
    ) values (
        'Usuario sin rol', 'sin.rol.admin.test@virtual-insurance.test',
        rpad('$2y$12$test', 60, 'x'), 'S', systimestamp
    ) returning usuario_interno_id into v_sin_rol_id;

    select cliente_id into v_cliente_id
      from cliente
     where email = 'emmanuel.demo@virtual-insurance.test';
    select tipo_tarjeta_id into v_tipo_tarjeta_id from tipo_tarjeta where clave = 'CREDIT';
    select modalidad_pago_id into v_modalidad_id from modalidad_pago where clave = 'MONTHLY';
    select estatus_intento_pago_id into v_intento_aprobado_id
      from estatus_intento_pago where clave = 'APROBADO';
    select estatus_poliza_id into v_poliza_activa_id from estatus_poliza where clave = 'ACTIVA';
    select estatus_cancelacion_id into v_cancelacion_sol_id
      from estatus_cancelacion where clave = 'SOLICITADA';
    select estatus_cancelacion_id into v_cancelacion_apr_id
      from estatus_cancelacion where clave = 'APROBADA';
    select estatus_cancelacion_id into v_cancelacion_rech_id
      from estatus_cancelacion where clave = 'RECHAZADA';
    select cotizacion_id into v_cotizacion_id from cotizacion where numero_cotizacion = 2;
    select seq_numero_pago.nextval into v_numero_pago from dual;

    insert into tarjeta (
        cliente_id, tipo_tarjeta_id, numero_tarjeta, anio_expiracion, mes_expiracion
    ) values (
        v_cliente_id, v_tipo_tarjeta_id,
        hextoraw('2122232425262728292A2B2C2D2E2F30'), 2028, 12
    ) returning tarjeta_id into v_tarjeta_id;

    insert into intento_pago (
        cotizacion_id, tarjeta_id, modalidad_pago_id, numero_pago,
        fecha_pago, importe, comprobante_pdf, estatus_intento_pago_id,
        motivo_rechazo, idempotency_key, codigo_respuesta, codigo_autorizacion
    ) values (
        v_cotizacion_id, v_tarjeta_id, v_modalidad_id, v_numero_pago,
        systimestamp, 1040.00, empty_blob(), v_intento_aprobado_id,
        null, 'ADMIN-CANCEL-TEST-0001', 'APPROVED', 'AUTH-ADMIN-TEST'
    ) returning intento_pago_id into v_intento_pago_id;

    insert into poliza (
        intento_pago_id, folio, vigencia_desde, vigencia_hasta,
        costo_seguro, iva_importe, folio_factura, factura_json,
        factura_xml, modalidad_pago_id, poliza_anterior_id, estatus_poliza_id
    ) values (
        v_intento_pago_id, 'VI-ADMIN-CAN-TEST', trunc(current_date),
        add_months(trunc(current_date), 12), 12480.00, 1996.80,
        'FAC-ADMIN-CAN-TEST', json('{"folio":"FAC-ADMIN-CAN-TEST"}'),
        xmltype('<factura><folio>FAC-ADMIN-CAN-TEST</folio></factura>'),
        v_modalidad_id, null, v_poliza_activa_id
    ) returning poliza_id into v_poliza_id;

    insert into solicitud_cancelacion (
        poliza_id, folio, motivo, fecha_solicitada, comentarios,
        estatus_cancelacion_id, fecha_creacion
    ) values (
        v_poliza_id, 'CAN-ADMIN-TEST-001', 'FINANCIAL', trunc(current_date),
        'prueba de rechazo administrativo', v_cancelacion_sol_id, systimestamp
    ) returning solicitud_cancelacion_id into v_solicitud_id;

    begin
        update solicitud_cancelacion
           set estatus_cancelacion_id = v_cancelacion_rech_id,
               resuelta_por_usuario_id = v_admin_id
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20121, 'se permitio resolver sin observacion.');
    exception
        when others then
            if sqlcode <> -20089 then raise; end if;
    end;

    begin
        update solicitud_cancelacion
           set estatus_cancelacion_id = v_cancelacion_rech_id,
               observacion_resolucion = 'sin responsable'
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20122, 'se permitio resolver sin responsable.');
    exception
        when others then
            if sqlcode <> -20094 then raise; end if;
    end;

    begin
        update solicitud_cancelacion
           set estatus_cancelacion_id = v_cancelacion_rech_id,
               observacion_resolucion = 'usuario inactivo',
               resuelta_por_usuario_id = v_inactivo_id
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20123, 'se permitio resolver con usuario inactivo.');
    exception
        when others then
            if sqlcode <> -20095 then raise; end if;
    end;

    begin
        update solicitud_cancelacion
           set estatus_cancelacion_id = v_cancelacion_rech_id,
               observacion_resolucion = 'usuario sin rol',
               resuelta_por_usuario_id = v_sin_rol_id
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20124, 'se permitio resolver con usuario sin rol.');
    exception
        when others then
            if sqlcode <> -20095 then raise; end if;
    end;

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_rech_id,
           observacion_resolucion = 'rechazo administrativo valido',
           resuelta_por_usuario_id = v_admin_id
     where solicitud_cancelacion_id = v_solicitud_id;

    select ep.clave into v_clave
      from poliza p
      join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
     where p.poliza_id = v_poliza_id;
    verificar(v_clave = 'ACTIVA', 'el rechazo debe devolver la poliza a activa');

    insert into solicitud_cancelacion (
        poliza_id, folio, motivo, fecha_solicitada, comentarios,
        estatus_cancelacion_id, fecha_creacion
    ) values (
        v_poliza_id, 'CAN-ADMIN-TEST-002', 'NOT_NEEDED', trunc(current_date),
        'prueba de aprobacion administrativa', v_cancelacion_sol_id, systimestamp
    ) returning solicitud_cancelacion_id into v_solicitud_id;

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_apr_id,
           observacion_resolucion = 'aprobacion administrativa valida',
           resuelta_por_usuario_id = v_admin_id
     where solicitud_cancelacion_id = v_solicitud_id;

    select ep.clave into v_clave
      from poliza p
      join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
     where p.poliza_id = v_poliza_id;
    verificar(v_clave = 'CANCELADA', 'la aprobacion debe cancelar inmediatamente la poliza');

    select count(*) into v_actual
      from solicitud_cancelacion
     where solicitud_cancelacion_id = v_solicitud_id
       and fecha_resolucion is not null
       and observacion_resolucion = 'aprobacion administrativa valida'
       and resuelta_por_usuario_id = v_admin_id;
    verificar(v_actual = 1, 'la resolucion debe conservar fecha, observacion y responsable');

    begin
        update solicitud_cancelacion
           set estatus_cancelacion_id = v_cancelacion_rech_id
         where solicitud_cancelacion_id = v_solicitud_id;
        raise_application_error(-20125, 'se permitio resolver dos veces una solicitud.');
    exception
        when others then
            if sqlcode <> -20086 then raise; end if;
    end;

    rollback;
    dbms_output.put_line('pruebas de administracion de cancelaciones completadas correctamente.');
exception
    when others then
        rollback;
        raise;
end;
/

select count(*) usuarios_internos_prueba_persistidos
  from usuario_interno
 where email like '%.admin.test@virtual-insurance.test';

select count(*) polizas_admin_cancelacion_prueba_persistidas
  from poliza
 where folio = 'VI-ADMIN-CAN-TEST';
