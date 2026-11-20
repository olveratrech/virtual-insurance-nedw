-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: cargar cotizaciones academicas aprobadas y rechazadas.
-- conexion requerida: vi_app@etobda_s1

set serveroutput on

declare
    v_usuario       varchar2(128);
    v_contenedor    varchar2(128);
    v_cliente_id    cliente.cliente_id%type;
    v_aprobada_id   estatus_cotizacion.estatus_cotizacion_id%type;
    v_rechazada_id  estatus_cotizacion.estatus_cotizacion_id%type;
    v_cotizacion_id cotizacion.cotizacion_id%type;

    procedure asegurar_cotizacion(
        p_numero       cotizacion.numero_cotizacion%type,
        p_fecha        cotizacion.fecha_cotizacion%type,
        p_vigencia     cotizacion.vigencia_cotizacion%type,
        p_tipo         cotizacion.tipo_seguro%type,
        p_costo        cotizacion.costo_calculado%type,
        p_estatus_id   cotizacion.estatus_cotizacion_id%type,
        p_cotizacion_id out cotizacion.cotizacion_id%type
    ) is
    begin
        begin
            select cotizacion_id
              into p_cotizacion_id
              from cotizacion
             where numero_cotizacion = p_numero;

            update cotizacion
               set cliente_id = v_cliente_id,
                   fecha_cotizacion = p_fecha,
                   vigencia_cotizacion = p_vigencia,
                   tipo_seguro = p_tipo,
                   costo_calculado = p_costo,
                   rechazo_pdf = null,
                   estatus_cotizacion_id = p_estatus_id
             where cotizacion_id = p_cotizacion_id;

            dbms_output.put_line('actualizada: cotizacion ' || to_char(p_numero, 'fm0000'));
        exception
            when no_data_found then
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
                    p_fecha,
                    p_vigencia,
                    p_tipo,
                    p_costo,
                    null,
                    p_estatus_id
                )
                returning cotizacion_id into p_cotizacion_id;

                dbms_output.put_line('creada: cotizacion ' || to_char(p_numero, 'fm0000'));
        end;
    end asegurar_cotizacion;
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

    select cliente_id into v_cliente_id
      from cliente
     where email = 'emmanuel.demo@virtual-insurance.test';

    select estatus_cotizacion_id into v_aprobada_id
      from estatus_cotizacion
     where clave = 'APROBADA';

    select estatus_cotizacion_id into v_rechazada_id
      from estatus_cotizacion
     where clave = 'RECHAZADA';

    asegurar_cotizacion(
        2,
        timestamp '2026-11-20 10:00:00',
        date '2026-12-20',
        'A',
        12480.00,
        v_aprobada_id,
        v_cotizacion_id
    );

    merge into cotizacion_auto destino
    using (select v_cotizacion_id cotizacion_id from dual) fuente
       on (destino.cotizacion_id = fuente.cotizacion_id)
    when matched then update set
        destino.marca = 'Mazda',
        destino.modelo = '3',
        destino.anio = 2024,
        destino.categoria = 'SEDAN',
        destino.placas = 'ABC123A',
        destino.numero_licencia = 'LIC-12345',
        destino.edad_conductor = 26,
        destino.puntos_negativos = 0,
        destino.antecedentes_penales = 'N'
    when not matched then insert (
        cotizacion_id, marca, modelo, anio, categoria, placas,
        numero_licencia, edad_conductor, puntos_negativos, antecedentes_penales
    ) values (
        fuente.cotizacion_id, 'Mazda', '3', 2024, 'SEDAN', 'ABC123A',
        'LIC-12345', 26, 0, 'N'
    );

    asegurar_cotizacion(
        3,
        timestamp '2026-11-20 10:05:00',
        date '2026-12-20',
        'M',
        18600.00,
        v_aprobada_id,
        v_cotizacion_id
    );

    merge into cotizacion_medica destino
    using (select v_cotizacion_id cotizacion_id from dual) fuente
       on (destino.cotizacion_id = fuente.cotizacion_id)
    when matched then update set
        destino.edad_asegurado = 35,
        destino.hipertension = 'N',
        destino.glucosa_fuera_rango = 'N',
        destino.colesterol_fuera_rango = 'N',
        destino.fumador = 'N',
        destino.adiccion_alcohol = 'N'
    when not matched then insert (
        cotizacion_id, edad_asegurado, hipertension, glucosa_fuera_rango,
        colesterol_fuera_rango, fumador, adiccion_alcohol
    ) values (
        fuente.cotizacion_id, 35, 'N', 'N', 'N', 'N', 'N'
    );

    asegurar_cotizacion(
        4,
        timestamp '2026-11-20 10:10:00',
        date '2026-12-20',
        'A',
        null,
        v_rechazada_id,
        v_cotizacion_id
    );

    merge into cotizacion_auto destino
    using (select v_cotizacion_id cotizacion_id from dual) fuente
       on (destino.cotizacion_id = fuente.cotizacion_id)
    when matched then update set
        destino.marca = 'Ford',
        destino.modelo = 'Focus',
        destino.anio = 2018,
        destino.categoria = 'SEDAN',
        destino.placas = 'XYZ987B',
        destino.numero_licencia = 'LIC-67890',
        destino.edad_conductor = 30,
        destino.puntos_negativos = 12,
        destino.antecedentes_penales = 'N'
    when not matched then insert (
        cotizacion_id, marca, modelo, anio, categoria, placas,
        numero_licencia, edad_conductor, puntos_negativos, antecedentes_penales
    ) values (
        fuente.cotizacion_id, 'Ford', 'Focus', 2018, 'SEDAN', 'XYZ987B',
        'LIC-67890', 30, 12, 'N'
    );

    asegurar_cotizacion(
        5,
        timestamp '2026-11-20 10:15:00',
        date '2026-12-20',
        'M',
        null,
        v_rechazada_id,
        v_cotizacion_id
    );

    merge into cotizacion_medica destino
    using (select v_cotizacion_id cotizacion_id from dual) fuente
       on (destino.cotizacion_id = fuente.cotizacion_id)
    when matched then update set
        destino.edad_asegurado = 72,
        destino.hipertension = 'N',
        destino.glucosa_fuera_rango = 'N',
        destino.colesterol_fuera_rango = 'N',
        destino.fumador = 'N',
        destino.adiccion_alcohol = 'N'
    when not matched then insert (
        cotizacion_id, edad_asegurado, hipertension, glucosa_fuera_rango,
        colesterol_fuera_rango, fumador, adiccion_alcohol
    ) values (
        fuente.cotizacion_id, 72, 'N', 'N', 'N', 'N', 'N'
    );

    commit;
end;
/

