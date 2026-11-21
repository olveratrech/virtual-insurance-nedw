-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar la estructura y los escenarios de la pasarela academica.
-- conexion requerida: vi_app@etobda_s1

set serveroutput on

declare
    v_actual number;

    procedure verificar(p_condicion boolean, p_mensaje varchar2) is
    begin
        if not p_condicion then
            raise_application_error(-20120, p_mensaje);
        end if;
    end;
begin
    verificar(
        sys_context('USERENV', 'CURRENT_USER') = 'VI_APP'
        and sys_context('USERENV', 'CON_NAME') = 'ETOBDA_S1',
        'conexion incorrecta'
    );

    select count(*) into v_actual
      from user_tables
     where table_name = 'INSTRUMENTO_PAGO_PRUEBA';
    verificar(v_actual = 1, 'falta instrumento_pago_prueba');

    select count(*) into v_actual
      from user_tab_columns
     where table_name = 'INTENTO_PAGO'
       and column_name in ('IDEMPOTENCY_KEY', 'CODIGO_RESPUESTA', 'CODIGO_AUTORIZACION');
    verificar(v_actual = 3, 'faltan columnas de resultado o idempotencia');

    select count(*) into v_actual
      from user_tab_columns
     where table_name = 'INTENTO_PAGO'
       and column_name = 'NUMERO_PAGO'
       and data_precision = 10
       and data_scale = 0;
    verificar(v_actual = 1, 'numero_pago debe admitir la secuencia de diez digitos');

    select count(*) into v_actual
      from user_sequences
     where sequence_name in ('SEQ_NUMERO_PAGO', 'SEQ_FOLIO_POLIZA');
    verificar(v_actual = 2, 'faltan secuencias de pago o poliza');

    select count(*) into v_actual from instrumento_pago_prueba;
    verificar(v_actual = 8, 'se esperaban 8 instrumentos de evaluacion');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where numero_hash = lower(rawtohex(standard_hash('4242424242421111', 'SHA256')))
       and saldo_disponible = 50000
       and estatus = 'ACTIVE';
    verificar(v_actual = 1, 'el instrumento con fondos no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where ultimos_cuatro = '2222'
       and saldo_disponible = 100;
    verificar(v_actual = 1, 'el instrumento sin fondos no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where ultimos_cuatro = '3333' and estatus = 'BLOCKED';
    verificar(v_actual = 1, 'el instrumento bloqueado no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where ultimos_cuatro = '4444'
       and anio_expiracion < extract(year from sysdate);
    verificar(v_actual = 1, 'el instrumento vencido no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where ultimos_cuatro = '5555'
       and respuesta_forzada = 'PROCESSOR_DECLINED';
    verificar(v_actual = 1, 'el rechazo forzado no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where numero_hash = lower(rawtohex(standard_hash('5105105105105100', 'SHA256')))
       and ultimos_cuatro = '5100'
       and estatus = 'ACTIVE';
    verificar(v_actual = 1, 'el instrumento Mastercard no coincide');

    select count(*) into v_actual
      from instrumento_pago_prueba
     where numero_hash = lower(rawtohex(standard_hash('371449635398431', 'SHA256')))
       and ultimos_cuatro = '8431'
       and estatus = 'ACTIVE';
    verificar(v_actual = 1, 'el instrumento American Express no coincide');

    dbms_output.put_line('prueba exitosa: procesador listo con 8 instrumentos.');
end;
/

