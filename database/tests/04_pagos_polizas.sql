-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar pagos, polizas, historial, documentos y reglas cruzadas.
-- conexion requerida: vi_app@etobda_s1

set serveroutput on

declare
    v_usuario            varchar2(128);
    v_contenedor         varchar2(128);
    v_actual             number;
    v_cliente_id         cliente.cliente_id%type;
    v_tipo_tarjeta_id    tipo_tarjeta.tipo_tarjeta_id%type;
    v_tarjeta_id         tarjeta.tarjeta_id%type;
    v_modalidad_id       modalidad_pago.modalidad_pago_id%type;
    v_aprobado_id        estatus_intento_pago.estatus_intento_pago_id%type;
    v_rechazado_id       estatus_intento_pago.estatus_intento_pago_id%type;
    v_activa_id          estatus_poliza.estatus_poliza_id%type;
    v_vencida_id         estatus_poliza.estatus_poliza_id%type;
    v_cot_aprobada_id    cotizacion.cotizacion_id%type;
    v_cot_rechazada_id   cotizacion.cotizacion_id%type;
    v_intento_aprobado   intento_pago.intento_pago_id%type;
    v_intento_rechazado  intento_pago.intento_pago_id%type;
    v_poliza_id          poliza.poliza_id%type;

    procedure verificar(
        p_condicion boolean,
        p_mensaje   varchar2
    ) is
    begin
        if not p_condicion then
            raise_application_error(-20070, p_mensaje);
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
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
     );
    verificar(v_actual = 4, 'se esperaban 4 tablas de pagos y polizas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_tab_columns
     where table_name in (
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
     );
    verificar(v_actual = 35, 'se esperaban 35 columnas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_tab_identity_cols
     where table_name in (
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
     );
    verificar(v_actual = 4, 'se esperaban 4 columnas identity y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'P'
       and table_name in (
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
       );
    verificar(v_actual = 4, 'se esperaban 4 llaves primarias y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'R'
       and table_name in (
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
       )
       and status = 'ENABLED';
    verificar(v_actual = 11, 'se esperaban 11 llaves foraneas habilitadas y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_type = 'U'
       and table_name in (
        'INTENTO_PAGO', 'POLIZA', 'HISTORICO_ESTATUS_POLIZA', 'DOCUMENTO_POLIZA'
       );
    verificar(v_actual = 6, 'se esperaban 6 restricciones unique y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_constraints
     where constraint_name in (
        'CK_INTENTO_PAGO_NUMERO',
        'CK_INTENTO_PAGO_IMPORTE',
        'CK_POLIZA_VIGENCIA',
        'CK_POLIZA_COSTO',
        'CK_POLIZA_IVA',
        'CK_HISTORICO_ESTATUS_POL_NUMERO',
        'CK_DOCUMENTO_POLIZA_TIPO',
        'CK_DOCUMENTO_POLIZA_MIME'
     )
       and constraint_type = 'C'
       and status = 'ENABLED';
    verificar(v_actual = 8, 'se esperaban 8 restricciones check y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_indexes
     where index_name in (
        'IX_INTENTO_PAGO_COTIZACION',
        'IX_INTENTO_PAGO_TARJETA',
        'IX_INTENTO_PAGO_MODALIDAD',
        'IX_INTENTO_PAGO_ESTATUS',
        'IX_POLIZA_MODALIDAD',
        'IX_POLIZA_ESTATUS',
        'IX_POLIZA_ANTERIOR',
        'IX_HISTORICO_POL_ESTATUS',
        'IX_DOCUMENTO_POLIZA_POLIZA'
     );
    verificar(v_actual = 9, 'se esperaban 9 indices adicionales y se encontraron ' || v_actual);

    select count(*) into v_actual
      from user_objects
     where object_name in (
        'TRG_INTENTO_PAGO_REGLAS',
        'TRG_POLIZA_REGLAS',
        'TRG_POLIZA_HISTORIAL'
     )
       and object_type = 'TRIGGER'
       and status = 'VALID';
    verificar(v_actual = 3, 'los 3 triggers deben estar compilados como validos');

    select cliente_id into v_cliente_id
      from cliente
     where email = 'emmanuel.demo@virtual-insurance.test';

    select tipo_tarjeta_id into v_tipo_tarjeta_id
      from tipo_tarjeta
     where clave = 'CREDIT';

    select modalidad_pago_id into v_modalidad_id
      from modalidad_pago
     where clave = 'MONTHLY';

    select estatus_intento_pago_id into v_aprobado_id
      from estatus_intento_pago
     where clave = 'APROBADO';

    select estatus_intento_pago_id into v_rechazado_id
      from estatus_intento_pago
     where clave = 'RECHAZADO';

    select estatus_poliza_id into v_activa_id
      from estatus_poliza
     where clave = 'ACTIVA';

    select estatus_poliza_id into v_vencida_id
      from estatus_poliza
     where clave = 'VENCIDA';

    select cotizacion_id into v_cot_aprobada_id
      from cotizacion
     where numero_cotizacion = 2;

    select cotizacion_id into v_cot_rechazada_id
      from cotizacion
     where numero_cotizacion = 4;

    insert into tarjeta (
        cliente_id,
        tipo_tarjeta_id,
        numero_tarjeta,
        anio_expiracion,
        mes_expiracion
    ) values (
        v_cliente_id,
        v_tipo_tarjeta_id,
        hextoraw('0102030405060708090A0B0C0D0E0F10'),
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
        v_cot_aprobada_id,
        v_tarjeta_id,
        v_modalidad_id,
        901,
        systimestamp,
        1040.00,
        null,
        v_rechazado_id,
        'rechazo academico de prueba'
    ) returning intento_pago_id into v_intento_rechazado;

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
        v_cot_aprobada_id,
        v_tarjeta_id,
        v_modalidad_id,
        902,
        systimestamp,
        1040.00,
        empty_blob(),
        v_aprobado_id,
        null
    ) returning intento_pago_id into v_intento_aprobado;

    begin
        insert into intento_pago (
            cotizacion_id, tarjeta_id, modalidad_pago_id, numero_pago,
            fecha_pago, importe, comprobante_pdf,
            estatus_intento_pago_id, motivo_rechazo
        ) values (
            v_cot_aprobada_id, v_tarjeta_id, v_modalidad_id, 903,
            systimestamp, 1040.00, empty_blob(), v_aprobado_id, null
        );
        raise_application_error(-20071, 'se permitio un segundo intento aprobado para la cotizacion.');
    exception
        when others then
            if sqlcode <> -20064 then
                raise;
            end if;
    end;

    begin
        insert into intento_pago (
            cotizacion_id, tarjeta_id, modalidad_pago_id, numero_pago,
            fecha_pago, importe, comprobante_pdf,
            estatus_intento_pago_id, motivo_rechazo
        ) values (
            v_cot_rechazada_id, v_tarjeta_id, v_modalidad_id, 904,
            systimestamp, 1040.00, null, v_rechazado_id, 'prueba'
        );
        raise_application_error(-20072, 'se permitio pagar una cotizacion rechazada.');
    exception
        when others then
            if sqlcode <> -20060 then
                raise;
            end if;
    end;

    begin
        insert into poliza (
            intento_pago_id, folio, vigencia_desde, vigencia_hasta,
            costo_seguro, iva_importe, folio_factura, factura_json,
            factura_xml, modalidad_pago_id, poliza_anterior_id, estatus_poliza_id
        ) values (
            v_intento_rechazado, 'VI-INVALIDA', date '2026-11-26', date '2027-11-25',
            12480.00, 1996.80, 'FAC-INVALIDA', json('{"prueba":true}'),
            xmltype('<factura><prueba>true</prueba></factura>'),
            v_modalidad_id, null, v_activa_id
        );
        raise_application_error(-20073, 'se permitio emitir desde un intento rechazado.');
    exception
        when others then
            if sqlcode <> -20065 then
                raise;
            end if;
    end;

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
        v_intento_aprobado,
        'VI-TEST-001',
        date '2026-11-26',
        date '2027-11-25',
        12480.00,
        1996.80,
        'FAC-TEST-001',
        json('{"folio":"FAC-TEST-001"}'),
        xmltype('<factura><folio>FAC-TEST-001</folio></factura>'),
        v_modalidad_id,
        null,
        v_activa_id
    ) returning poliza_id into v_poliza_id;

    insert into documento_poliza (
        poliza_id,
        tipo_documento,
        nombre_archivo,
        contenido,
        tipo_mime,
        fecha_generacion
    ) values (
        v_poliza_id,
        'POLICY_PDF',
        'poliza-VI-TEST-001.pdf',
        empty_blob(),
        'application/pdf',
        systimestamp
    );

    select count(*) into v_actual
      from historico_estatus_poliza
     where poliza_id = v_poliza_id;
    verificar(v_actual = 1, 'una poliza nueva debe tener un registro historico inicial');

    update poliza
       set estatus_poliza_id = v_vencida_id
     where poliza_id = v_poliza_id;

    select count(*) into v_actual
      from historico_estatus_poliza
     where poliza_id = v_poliza_id;
    verificar(v_actual = 2, 'el cambio de estatus debe generar un segundo historico');

    rollback;
    dbms_output.put_line('pruebas de pagos y polizas completadas correctamente.');
exception
    when others then
        rollback;
        raise;
end;
/

select count(*) intentos_prueba_persistidos
  from intento_pago
 where numero_pago between 901 and 904;

select count(*) polizas_prueba_persistidas
  from poliza
 where folio like 'VI-TEST-%';

