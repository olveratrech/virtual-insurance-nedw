-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: verificar cliente, tarjeta, relaciones, indices y datos iniciales.
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
            raise_application_error(-20030, p_mensaje);
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
     where table_name in ('CLIENTE', 'TARJETA');
    verificar(v_actual = 2, 'se esperaban cliente y tarjeta; se encontraron ' || v_actual || ' tablas');

    select count(*)
      into v_actual
      from user_tab_columns
     where table_name in ('CLIENTE', 'TARJETA');
    verificar(v_actual = 17, 'se esperaban 17 columnas y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_tab_identity_cols
     where table_name in ('CLIENTE', 'TARJETA');
    verificar(v_actual = 2, 'se esperaban 2 columnas identity y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'P'
       and table_name in ('CLIENTE', 'TARJETA');
    verificar(v_actual = 2, 'se esperaban 2 llaves primarias y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'R'
       and table_name in ('CLIENTE', 'TARJETA')
       and status = 'ENABLED';
    verificar(v_actual = 3, 'se esperaban 3 llaves foraneas habilitadas y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_type = 'U'
       and table_name = 'CLIENTE';
    verificar(v_actual = 2, 'cliente debe tener 2 restricciones unique y tiene ' || v_actual);

    select count(*)
      into v_actual
      from user_constraints
     where constraint_name in (
        'CK_CLIENTE_SEXO',
        'CK_CLIENTE_FECHA_NACIMIENTO',
        'CK_CLIENTE_FISCAL_NORMALIZADO',
        'CK_CLIENTE_EMAIL_NORMALIZADO',
        'CK_CLIENTE_CONTRASENA_HASH',
        'CK_TARJETA_ANIO_EXPIRACION',
        'CK_TARJETA_MES_EXPIRACION'
     )
       and constraint_type = 'C'
       and status = 'ENABLED';
    verificar(v_actual = 7, 'se esperaban 7 restricciones check y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_indexes
     where index_name in (
        'IX_CLIENTE_GENERO',
        'IX_TARJETA_CLIENTE',
        'IX_TARJETA_TIPO'
     );
    verificar(v_actual = 3, 'se esperaban 3 indices de llaves foraneas y se encontraron ' || v_actual);

    select count(*)
      into v_actual
      from user_tab_columns
     where table_name = 'TARJETA'
       and column_name = 'NUMERO_TARJETA'
       and data_type = 'RAW'
       and data_length = 128;
    verificar(v_actual = 1, 'numero_tarjeta debe ser raw(128)');

    select count(*)
      into v_actual
      from user_tab_columns
     where table_name = 'CLIENTE'
       and column_name = 'FOTO'
       and data_type = 'BLOB';
    verificar(v_actual = 1, 'foto debe ser blob');

    select count(*)
      into v_actual
      from cliente
     where email = 'emmanuel.demo@virtual-insurance.test'
       and length(contrasena) >= 60
       and dbms_lob.getlength(foto) > 0;
    verificar(v_actual = 1, 'falta el cliente de demostracion o sus datos protegidos');

    dbms_output.put_line('pruebas de clientes y tarjetas completadas correctamente.');
end;
/

select cliente_id,
       nombre,
       apellido_paterno,
       apellido_materno,
       email,
       sexo,
       identificador_fiscal,
       dbms_lob.getlength(foto) foto_bytes
  from cliente
 where email = 'emmanuel.demo@virtual-insurance.test';

