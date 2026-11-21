-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: probar de extremo a extremo los recorridos de negocio en Oracle.
-- conexion requerida: vi_app@etobda_s1
-- los datos de esta prueba son temporales y terminan con rollback.

set serveroutput on
set feedback on
set linesize 180
set pagesize 100

spool salida_recorrido_e2e.txt replace

prompt ============================================================
prompt Virtual Insurance - inicio de la prueba E2E transaccional
prompt Los datos creados durante el recorrido terminaran en rollback.
prompt ============================================================

declare
    v_usuario                varchar2(128);
    v_contenedor             varchar2(128);
    v_actual                 number;
    v_cliente_id             cliente.cliente_id%type;
    v_genero_id              genero.genero_id%type;
    v_tipo_tarjeta_id        tipo_tarjeta.tipo_tarjeta_id%type;
    v_tarjeta_id             tarjeta.tarjeta_id%type;
    v_modalidad_id           modalidad_pago.modalidad_pago_id%type;
    v_cot_aprobada_id        estatus_cotizacion.estatus_cotizacion_id%type;
    v_cot_rechazada_id       estatus_cotizacion.estatus_cotizacion_id%type;
    v_pago_aprobado_id       estatus_intento_pago.estatus_intento_pago_id%type;
    v_pago_rechazado_id      estatus_intento_pago.estatus_intento_pago_id%type;
    v_poliza_activa_id       estatus_poliza.estatus_poliza_id%type;
    v_cancelacion_sol_id     estatus_cancelacion.estatus_cancelacion_id%type;
    v_cancelacion_apr_id     estatus_cancelacion.estatus_cancelacion_id%type;
    v_cancelacion_rech_id    estatus_cancelacion.estatus_cancelacion_id%type;
    v_auto_rechazada_id      cotizacion.cotizacion_id%type;
    v_medica_rechazada_id    cotizacion.cotizacion_id%type;
    v_auto_pago_rech_id      cotizacion.cotizacion_id%type;
    v_medica_pago_rech_id    cotizacion.cotizacion_id%type;
    v_auto_emitida_id        cotizacion.cotizacion_id%type;
    v_medica_emitida_id      cotizacion.cotizacion_id%type;
    v_intento_auto_rech_id   intento_pago.intento_pago_id%type;
    v_intento_med_rech_id    intento_pago.intento_pago_id%type;
    v_intento_auto_apr_id    intento_pago.intento_pago_id%type;
    v_intento_med_apr_id     intento_pago.intento_pago_id%type;
    v_poliza_auto_id         poliza.poliza_id%type;
    v_poliza_medica_id       poliza.poliza_id%type;
    v_cancelacion_auto_id    solicitud_cancelacion.solicitud_cancelacion_id%type;
    v_cancelacion_medica_id  solicitud_cancelacion.solicitud_cancelacion_id%type;
    v_admin_id               usuario_interno.usuario_interno_id%type;
    v_clave                  varchar2(30);

    procedure verificar(
        p_condicion boolean,
        p_mensaje   varchar2
    ) is
    begin
        if not p_condicion then
            raise_application_error(-20100, p_mensaje);
        end if;
    end verificar;

    procedure iniciar_escenario(p_nombre varchar2) is
    begin
        dbms_output.put_line(chr(10) || '--- ' || p_nombre || ' ---');
    end iniciar_escenario;

    procedure aprobar_escenario(p_mensaje varchar2) is
    begin
        dbms_output.put_line('[correcto] ' || p_mensaje);
    end aprobar_escenario;

    procedure mostrar_paso(p_mensaje varchar2) is
    begin
        dbms_output.put_line('  -> ' || p_mensaje);
    end mostrar_paso;

    procedure confirmar_rechazo(
        p_codigo  number,
        p_mensaje varchar2
    ) is
    begin
        dbms_output.put_line(
            '  [rechazo esperado ' || to_char(p_codigo) || '] ' || p_mensaje
        );
    end confirmar_rechazo;

    procedure crear_cotizacion(
        p_numero       cotizacion.numero_cotizacion%type,
        p_tipo         cotizacion.tipo_seguro%type,
        p_aprobada     boolean,
        p_cotizacion_id out cotizacion.cotizacion_id%type
    ) is
        v_estatus_id estatus_cotizacion.estatus_cotizacion_id%type;
        v_costo      cotizacion.costo_calculado%type;
        v_marca      cotizacion_auto.marca%type;
        v_modelo     cotizacion_auto.modelo%type;
        v_puntos     cotizacion_auto.puntos_negativos%type;
        v_edad       cotizacion_medica.edad_asegurado%type;
    begin
        if p_aprobada then
            v_estatus_id := v_cot_aprobada_id;
            v_costo := case p_tipo when 'A' then 12000 else 18000 end;
            v_marca := 'Mazda';
            v_modelo := '3';
            v_puntos := 0;
            v_edad := 35;
        else
            v_estatus_id := v_cot_rechazada_id;
            v_costo := null;
            v_marca := 'Ford';
            v_modelo := 'Focus';
            v_puntos := 12;
            v_edad := 72;
        end if;

        insert into cotizacion (
            cliente_id,
            numero_cotizacion,
            fecha_cotizacion,
            vigencia_cotizacion,
            tipo_seguro,
            costo_calculado,
            rechazo_pdf,
            estatus_cotizacion_id
        ) values (
            v_cliente_id,
            p_numero,
            systimestamp,
            trunc(current_date) + 30,
            p_tipo,
            v_costo,
            null,
            v_estatus_id
        ) returning cotizacion_id into p_cotizacion_id;

        if not p_aprobada then
            update cotizacion
               set rechazo_pdf = empty_blob()
             where cotizacion_id = p_cotizacion_id;
        end if;

        if p_tipo = 'A' then
            insert into cotizacion_auto (
                cotizacion_id, marca, modelo, anio, categoria, placas,
                numero_licencia, edad_conductor, puntos_negativos,
                antecedentes_penales
            ) values (
                p_cotizacion_id,
                v_marca,
                v_modelo,
                2024,
                'SEDAN',
                'E2E-' || to_char(p_numero),
                'LIC-E2E-' || to_char(p_numero),
                30,
                v_puntos,
                'N'
            );
        else
            insert into cotizacion_medica (
                cotizacion_id, edad_asegurado, hipertension,
                glucosa_fuera_rango, colesterol_fuera_rango,
                fumador, adiccion_alcohol
            ) values (
                p_cotizacion_id,
                v_edad,
                'N', 'N', 'N', 'N', 'N'
            );
        end if;

        mostrar_paso(
            'cotizacion ' || to_char(p_numero)
            || ' creada con id=' || to_char(p_cotizacion_id)
            || ', tipo=' || case p_tipo when 'A' then 'AUTO' else 'MEDICO' end
            || ', resultado=' || case when p_aprobada then 'APROBADA' else 'RECHAZADA' end
        );
    end crear_cotizacion;

    procedure crear_intento_rechazado(
        p_cotizacion_id cotizacion.cotizacion_id%type,
        p_numero_pago   intento_pago.numero_pago%type,
        p_importe       intento_pago.importe%type,
        p_intento_id    out intento_pago.intento_pago_id%type
    ) is
    begin
        insert into intento_pago (
            cotizacion_id, tarjeta_id, modalidad_pago_id, numero_pago,
            fecha_pago, importe, comprobante_pdf,
            estatus_intento_pago_id, motivo_rechazo
        ) values (
            p_cotizacion_id, v_tarjeta_id, v_modalidad_id, p_numero_pago,
            systimestamp, p_importe, null,
            v_pago_rechazado_id, 'rechazo simulado de la prueba e2e'
        ) returning intento_pago_id into p_intento_id;

        mostrar_paso(
            'intento de pago rechazado creado con id=' || to_char(p_intento_id)
            || ', numero=' || to_char(p_numero_pago)
        );
    end crear_intento_rechazado;

    procedure crear_intento_aprobado(
        p_cotizacion_id cotizacion.cotizacion_id%type,
        p_numero_pago   intento_pago.numero_pago%type,
        p_importe       intento_pago.importe%type,
        p_intento_id    out intento_pago.intento_pago_id%type
    ) is
    begin
        insert into intento_pago (
            cotizacion_id, tarjeta_id, modalidad_pago_id, numero_pago,
            fecha_pago, importe, comprobante_pdf,
            estatus_intento_pago_id, motivo_rechazo
        ) values (
            p_cotizacion_id, v_tarjeta_id, v_modalidad_id, p_numero_pago,
            systimestamp, p_importe, empty_blob(),
            v_pago_aprobado_id, null
        ) returning intento_pago_id into p_intento_id;

        mostrar_paso(
            'intento de pago aprobado creado con id=' || to_char(p_intento_id)
            || ', numero=' || to_char(p_numero_pago)
        );
    end crear_intento_aprobado;

    procedure emitir_poliza(
        p_intento_id intento_pago.intento_pago_id%type,
        p_folio      poliza.folio%type,
        p_costo      poliza.costo_seguro%type,
        p_poliza_id  out poliza.poliza_id%type
    ) is
    begin
        insert into poliza (
            intento_pago_id, folio, vigencia_desde, vigencia_hasta,
            costo_seguro, iva_importe, folio_factura, factura_json,
            factura_xml, modalidad_pago_id, poliza_anterior_id,
            estatus_poliza_id
        ) values (
            p_intento_id,
            p_folio,
            trunc(current_date) + 1,
            add_months(trunc(current_date) + 1, 12) - 1,
            p_costo,
            round(p_costo * 0.16, 2),
            'FAC-' || p_folio,
            json('{"origen":"prueba-e2e"}'),
            xmltype('<factura><origen>prueba-e2e</origen></factura>'),
            v_modalidad_id,
            null,
            v_poliza_activa_id
        ) returning poliza_id into p_poliza_id;

        insert into documento_poliza (
            poliza_id, tipo_documento, nombre_archivo,
            contenido, tipo_mime, fecha_generacion
        ) values (
            p_poliza_id, 'POLICY_PDF', lower(p_folio) || '.pdf',
            empty_blob(), 'application/pdf', systimestamp
        );

        insert into documento_poliza (
            poliza_id, tipo_documento, nombre_archivo,
            contenido, tipo_mime, fecha_generacion
        ) values (
            p_poliza_id, 'PAYMENT_RECEIPT', 'pago-' || lower(p_folio) || '.pdf',
            empty_blob(), 'application/pdf', systimestamp
        );

        mostrar_paso(
            'poliza ' || p_folio || ' emitida con id=' || to_char(p_poliza_id)
            || ' y dos documentos PDF'
        );
    end emitir_poliza;

    procedure verificar_estatus_poliza(
        p_poliza_id poliza.poliza_id%type,
        p_esperado  estatus_poliza.clave%type,
        p_mensaje   varchar2
    ) is
    begin
        select ep.clave into v_clave
          from poliza p
          join estatus_poliza ep
            on ep.estatus_poliza_id = p.estatus_poliza_id
         where p.poliza_id = p_poliza_id;

        verificar(v_clave = p_esperado, p_mensaje || '; se obtuvo ' || v_clave);
        mostrar_paso(
            'poliza id=' || to_char(p_poliza_id) || ' verificada en estado ' || v_clave
        );
    end verificar_estatus_poliza;
begin
    v_usuario := sys_context('USERENV', 'CURRENT_USER');
    v_contenedor := sys_context('USERENV', 'CON_NAME');

    verificar(
        v_usuario = 'VI_APP' and v_contenedor = 'ETOBDA_S1',
        'conexion incorrecta. se esperaba VI_APP@ETOBDA_S1 y se obtuvo '
        || v_usuario || '@' || v_contenedor
    );

    dbms_output.put_line('============================================================');
    dbms_output.put_line('Virtual Insurance - prueba E2E transaccional de Oracle');
    dbms_output.put_line('============================================================');

    select genero_id into v_genero_id from genero where clave = 'HOMBRE';
    select tipo_tarjeta_id into v_tipo_tarjeta_id from tipo_tarjeta where clave = 'CREDIT';
    select modalidad_pago_id into v_modalidad_id from modalidad_pago where clave = 'MONTHLY';
    select estatus_cotizacion_id into v_cot_aprobada_id
      from estatus_cotizacion where clave = 'APROBADA';
    select estatus_cotizacion_id into v_cot_rechazada_id
      from estatus_cotizacion where clave = 'RECHAZADA';
    select estatus_intento_pago_id into v_pago_aprobado_id
      from estatus_intento_pago where clave = 'APROBADO';
    select estatus_intento_pago_id into v_pago_rechazado_id
      from estatus_intento_pago where clave = 'RECHAZADO';
    select estatus_poliza_id into v_poliza_activa_id
      from estatus_poliza where clave = 'ACTIVA';
    select estatus_cancelacion_id into v_cancelacion_sol_id
      from estatus_cancelacion where clave = 'SOLICITADA';
    select estatus_cancelacion_id into v_cancelacion_apr_id
      from estatus_cancelacion where clave = 'APROBADA';
    select estatus_cancelacion_id into v_cancelacion_rech_id
      from estatus_cancelacion where clave = 'RECHAZADA';
    select usuario_interno_id into v_admin_id
      from usuario_interno
     where email = 'admin.cancelaciones@virtual-insurance.test';

    iniciar_escenario('preparacion del cliente y medio de pago');

    insert into cliente (
        nombre, apellido_paterno, apellido_materno, fecha_nacimiento,
        sexo, identificador_fiscal, email, contrasena, foto, genero_id
    ) values (
        'Cliente', 'Prueba', 'E2E', date '1995-05-20',
        'M', 'E2E951120ABC', 'cliente.e2e@virtual-insurance.test',
        '$2a$10$123456789012345678901u12345678901234567890123456789012',
        empty_blob(), v_genero_id
    ) returning cliente_id into v_cliente_id;

    insert into tarjeta (
        cliente_id, tipo_tarjeta_id, numero_tarjeta,
        anio_expiracion, mes_expiracion
    ) values (
        v_cliente_id, v_tipo_tarjeta_id,
        hextoraw('2122232425262728292A2B2C2D2E2F30'),
        2028, 12
    ) returning tarjeta_id into v_tarjeta_id;

    mostrar_paso(
        'cliente creado con id=' || to_char(v_cliente_id)
        || ' y correo cliente.e2e@virtual-insurance.test'
    );
    mostrar_paso(
        'tarjeta ficticia creada con id=' || to_char(v_tarjeta_id)
        || '; el CVV no se persiste'
    );

    aprobar_escenario('cliente registrado y tarjeta ficticia almacenada');

    iniciar_escenario('1. cotizacion de auto rechazada');
    crear_cotizacion(8101, 'A', false, v_auto_rechazada_id);

    begin
        crear_intento_rechazado(v_auto_rechazada_id, 811, 12000, v_intento_auto_rech_id);
        raise_application_error(-20101, 'se permitio pagar una cotizacion de auto rechazada.');
    exception
        when others then
            if sqlcode <> -20060 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio pagar la cotizacion de auto rechazada');
    end;
    aprobar_escenario('el rechazo se conserva y cualquier pago queda bloqueado');

    iniciar_escenario('2. cotizacion medica rechazada');
    crear_cotizacion(8102, 'M', false, v_medica_rechazada_id);

    begin
        crear_intento_rechazado(v_medica_rechazada_id, 812, 18000, v_intento_med_rech_id);
        raise_application_error(-20102, 'se permitio pagar una cotizacion medica rechazada.');
    exception
        when others then
            if sqlcode <> -20060 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio pagar la cotizacion medica rechazada');
    end;
    aprobar_escenario('el rechazo medico tampoco permite intentos de pago');

    iniciar_escenario('3. auto aprobado con pago rechazado');
    crear_cotizacion(8103, 'A', true, v_auto_pago_rech_id);
    crear_intento_rechazado(v_auto_pago_rech_id, 813, 12000, v_intento_auto_rech_id);

    select count(*) into v_actual
      from poliza p
      join intento_pago i on i.intento_pago_id = p.intento_pago_id
     where i.cotizacion_id = v_auto_pago_rech_id;
    verificar(v_actual = 0, 'un pago rechazado de auto no debe producir poliza');

    begin
        emitir_poliza(v_intento_auto_rech_id, 'VI-E2E-INVALIDA-A', 12000, v_poliza_auto_id);
        raise_application_error(-20103, 'se emitio una poliza desde un pago rechazado de auto.');
    exception
        when others then
            if sqlcode <> -20065 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio emitir desde el pago rechazado de auto');
    end;
    aprobar_escenario('el pago rechazado se registra y no permite emitir poliza');

    iniciar_escenario('4. medico aprobado con pago rechazado');
    crear_cotizacion(8104, 'M', true, v_medica_pago_rech_id);
    crear_intento_rechazado(v_medica_pago_rech_id, 814, 18000, v_intento_med_rech_id);

    select count(*) into v_actual
      from poliza p
      join intento_pago i on i.intento_pago_id = p.intento_pago_id
     where i.cotizacion_id = v_medica_pago_rech_id;
    verificar(v_actual = 0, 'un pago rechazado medico no debe producir poliza');
    mostrar_paso('consulta confirmada: el pago medico rechazado produjo cero polizas');
    aprobar_escenario('el pago rechazado medico queda registrado sin poliza');

    iniciar_escenario('5. auto emitido con cancelacion rechazada');
    crear_cotizacion(8105, 'A', true, v_auto_emitida_id);
    crear_intento_aprobado(v_auto_emitida_id, 815, 12000, v_intento_auto_apr_id);
    emitir_poliza(v_intento_auto_apr_id, 'VI-E2E-AUTO', 12000, v_poliza_auto_id);

    verificar_estatus_poliza(v_poliza_auto_id, 'ACTIVA', 'la poliza de auto debe iniciar activa');

    select count(*) into v_actual
      from documento_poliza
     where poliza_id = v_poliza_auto_id;
    verificar(v_actual = 2, 'la poliza de auto debe tener sus dos documentos');

    begin
        crear_intento_aprobado(v_auto_emitida_id, 816, 12000, v_intento_med_apr_id);
        raise_application_error(-20104, 'se permitio un segundo pago aprobado.');
    exception
        when others then
            if sqlcode <> -20064 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio un segundo pago aprobado para la cotizacion');
    end;

    begin
        insert into solicitud_cancelacion (
            poliza_id, folio, motivo, fecha_solicitada,
            comentarios, estatus_cancelacion_id, fecha_creacion
        ) values (
            v_poliza_auto_id, 'CAN-E2E-PASADA', 'FINANCIAL',
            trunc(current_date) - 1, null, v_cancelacion_sol_id, systimestamp
        );
        raise_application_error(-20105, 'se permitio una fecha de cancelacion pasada.');
    exception
        when others then
            if sqlcode <> -20083 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio solicitar una fecha de cancelacion pasada');
    end;

    insert into solicitud_cancelacion (
        poliza_id, folio, motivo, fecha_solicitada,
        comentarios, estatus_cancelacion_id, fecha_creacion
    ) values (
        v_poliza_auto_id, 'CAN-E2E-AUTO', 'FINANCIAL',
        trunc(current_date), 'solicitud e2e de auto',
        v_cancelacion_sol_id, systimestamp
    ) returning solicitud_cancelacion_id into v_cancelacion_auto_id;

    mostrar_paso(
        'cancelacion de auto creada con id=' || to_char(v_cancelacion_auto_id)
        || ' y estado SOLICITADA'
    );

    verificar_estatus_poliza(
        v_poliza_auto_id,
        'CANCELACION_SOLICITADA',
        'la solicitud debe cambiar el estado de la poliza de auto'
    );

    begin
        insert into solicitud_cancelacion (
            poliza_id, folio, motivo, fecha_solicitada,
            comentarios, estatus_cancelacion_id, fecha_creacion
        ) values (
            v_poliza_auto_id, 'CAN-E2E-AUTO-DUP', 'OTHER',
            trunc(current_date), null, v_cancelacion_sol_id, systimestamp
        );
        raise_application_error(-20106, 'se permitio duplicar una cancelacion pendiente.');
    exception
        when others then
            if sqlcode not in (-20082, -20088) then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio duplicar la cancelacion pendiente');
    end;

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_rech_id,
           observacion_resolucion = 'la cobertura permanece activa',
           resuelta_por_usuario_id = v_admin_id
     where solicitud_cancelacion_id = v_cancelacion_auto_id;

    mostrar_paso('la cancelacion de auto fue resuelta como RECHAZADA');

    verificar_estatus_poliza(
        v_poliza_auto_id,
        'ACTIVA',
        'una cancelacion rechazada debe devolver la poliza de auto a activa'
    );
    aprobar_escenario('emision, documentos, controles y rechazo de cancelacion correctos');

    iniciar_escenario('6. medico emitido con cancelacion aprobada');
    crear_cotizacion(8106, 'M', true, v_medica_emitida_id);
    crear_intento_aprobado(v_medica_emitida_id, 817, 18000, v_intento_med_apr_id);
    emitir_poliza(v_intento_med_apr_id, 'VI-E2E-MEDICA', 18000, v_poliza_medica_id);

    insert into solicitud_cancelacion (
        poliza_id, folio, motivo, fecha_solicitada,
        comentarios, estatus_cancelacion_id, fecha_creacion
    ) values (
        v_poliza_medica_id, 'CAN-E2E-MEDICA', 'NOT_NEEDED',
        trunc(current_date), 'solicitud e2e medica',
        v_cancelacion_sol_id, systimestamp
    ) returning solicitud_cancelacion_id into v_cancelacion_medica_id;

    mostrar_paso(
        'cancelacion medica creada con id=' || to_char(v_cancelacion_medica_id)
        || ' y estado SOLICITADA'
    );

    update solicitud_cancelacion
       set estatus_cancelacion_id = v_cancelacion_apr_id,
           observacion_resolucion = 'cancelacion aprobada en la prueba e2e',
           resuelta_por_usuario_id = v_admin_id
     where solicitud_cancelacion_id = v_cancelacion_medica_id;

    mostrar_paso('la cancelacion medica fue resuelta como APROBADA');

    verificar_estatus_poliza(
        v_poliza_medica_id,
        'CANCELADA',
        'una cancelacion aprobada debe cancelar la poliza medica'
    );

    begin
        insert into solicitud_cancelacion (
            poliza_id, folio, motivo, fecha_solicitada,
            comentarios, estatus_cancelacion_id, fecha_creacion
        ) values (
            v_poliza_medica_id, 'CAN-E2E-CANCELADA', 'OTHER',
            trunc(current_date), null, v_cancelacion_sol_id, systimestamp
        );
        raise_application_error(-20107, 'una poliza cancelada admitio otra solicitud.');
    exception
        when others then
            if sqlcode <> -20082 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio cancelar nuevamente una poliza cancelada');
    end;

    begin
        delete from solicitud_cancelacion
         where solicitud_cancelacion_id = v_cancelacion_medica_id;
        raise_application_error(-20108, 'se permitio eliminar una cancelacion.');
    exception
        when others then
            if sqlcode <> -20080 then
                raise;
            end if;
            confirmar_rechazo(sqlcode, 'Oracle impidio eliminar una solicitud de cancelacion');
    end;
    aprobar_escenario('emision medica, cancelacion definitiva y controles correctos');

    iniciar_escenario('verificacion integral');

    select count(*) into v_actual
      from cotizacion
     where cliente_id = v_cliente_id;
    verificar(v_actual = 6, 'el recorrido debe contener seis cotizaciones');
    mostrar_paso('resumen previo al rollback: 1 cliente y 6 cotizaciones');

    select count(*) into v_actual
      from historico_estatus_cotizacion h
      join cotizacion c on c.cotizacion_id = h.cotizacion_id
     where c.cliente_id = v_cliente_id;
    verificar(v_actual = 6, 'cada cotizacion debe tener su estado historico inicial');

    select count(*) into v_actual
      from intento_pago i
      join cotizacion c on c.cotizacion_id = i.cotizacion_id
     where c.cliente_id = v_cliente_id;
    verificar(v_actual = 4, 'deben persistir dos pagos rechazados y dos aprobados durante la prueba');
    mostrar_paso('resumen previo al rollback: 4 intentos de pago');

    select count(*) into v_actual
      from poliza p
      join intento_pago i on i.intento_pago_id = p.intento_pago_id
      join cotizacion c on c.cotizacion_id = i.cotizacion_id
     where c.cliente_id = v_cliente_id;
    verificar(v_actual = 2, 'solo los dos pagos aprobados deben generar poliza');
    mostrar_paso('resumen previo al rollback: 2 polizas');

    select count(*) into v_actual
      from documento_poliza d
      join poliza p on p.poliza_id = d.poliza_id
      join intento_pago i on i.intento_pago_id = p.intento_pago_id
      join cotizacion c on c.cotizacion_id = i.cotizacion_id
     where c.cliente_id = v_cliente_id;
    verificar(v_actual = 4, 'las dos polizas deben producir cuatro documentos');
    mostrar_paso('resumen previo al rollback: 4 documentos');

    select count(*) into v_actual
      from solicitud_cancelacion s
      join poliza p on p.poliza_id = s.poliza_id
      join intento_pago i on i.intento_pago_id = p.intento_pago_id
      join cotizacion c on c.cotizacion_id = i.cotizacion_id
     where c.cliente_id = v_cliente_id;
    verificar(v_actual = 2, 'deben existir dos solicitudes resueltas durante la prueba');
    mostrar_paso('resumen previo al rollback: 2 cancelaciones resueltas');

    select count(*) into v_actual
      from historico_estatus_poliza h
     where h.poliza_id in (v_poliza_auto_id, v_poliza_medica_id);
    verificar(v_actual = 6, 'cada poliza debe registrar emision, solicitud y resolucion');

    aprobar_escenario('conteos, relaciones, estados e historiales son coherentes');

    rollback;
    dbms_output.put_line(chr(10) || '============================================================');
    dbms_output.put_line('PRUEBA E2E COMPLETADA: 6 recorridos y controles negativos correctos');
    dbms_output.put_line('ROLLBACK COMPLETADO: no se conservaron datos temporales');
    dbms_output.put_line('============================================================');
exception
    when others then
        rollback;
        dbms_output.put_line(chr(10) || '[fallo] ' || sqlerrm);
        dbms_output.put_line('rollback ejecutado por fallo de la prueba e2e.');
        raise;
end;
/

prompt
prompt ============================================================
prompt Comprobacion posterior al rollback
prompt Cada una de las siguientes consultas debe devolver cero.
prompt ============================================================

select count(*) clientes_e2e_persistidos
  from cliente
 where email = 'cliente.e2e@virtual-insurance.test';

select count(*) cotizaciones_e2e_persistidas
  from cotizacion
 where numero_cotizacion between 8101 and 8106;

select count(*) pagos_e2e_persistidos
  from intento_pago
 where numero_pago between 811 and 817;

select count(*) polizas_e2e_persistidas
  from poliza
 where folio like 'VI-E2E-%';

select count(*) cancelaciones_e2e_persistidas
  from solicitud_cancelacion
 where folio like 'CAN-E2E-%';

prompt ============================================================
prompt Fin de la prueba E2E transaccional de Virtual Insurance
prompt ============================================================

spool off
