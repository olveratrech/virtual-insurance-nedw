-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: cargar un cliente academico sin almacenar tarjetas en texto plano.
-- conexion requerida: vi_app@etobda_s1
-- credenciales: emmanuel.demo@virtual-insurance.test / Virtual2026!

set serveroutput on

declare
    v_usuario      varchar2(128);
    v_contenedor   varchar2(128);
    v_genero_id    genero.genero_id%type;
    v_cliente_id   cliente.cliente_id%type;
    v_foto         blob;
    v_png          raw(32767) := hextoraw(
        '89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C489'
        || '0000000D4944415408D763F8CFC0F01F00050001FF89993D1D0000000049454E44AE426082'
    );
    v_hash         varchar2(255) :=
        '$2y$12$g3uTHlOAhqG6DCWzcIBVR.SJYNF2mwu1HRHm8H3mCk4bFfMbWYj1C';
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

    select genero_id
      into v_genero_id
      from genero
     where clave = 'HOMBRE';

    begin
        select cliente_id
          into v_cliente_id
          from cliente
         where email = 'emmanuel.demo@virtual-insurance.test';

        update cliente
           set nombre = 'Emmanuel',
               apellido_paterno = 'Trejo',
               apellido_materno = 'Olvera',
               fecha_nacimiento = date '1999-03-11',
               sexo = 'M',
               identificador_fiscal = 'ETO-DEMO-001',
               contrasena = v_hash,
               genero_id = v_genero_id
         where cliente_id = v_cliente_id;

        dbms_output.put_line('actualizado: cliente de demostracion');
    exception
        when no_data_found then
            insert into cliente (
                nombre,
                apellido_paterno,
                apellido_materno,
                fecha_nacimiento,
                sexo,
                identificador_fiscal,
                email,
                contrasena,
                foto,
                genero_id
            ) values (
                'Emmanuel',
                'Trejo',
                'Olvera',
                date '1999-03-11',
                'M',
                'ETO-DEMO-001',
                'emmanuel.demo@virtual-insurance.test',
                v_hash,
                empty_blob(),
                v_genero_id
            )
            returning cliente_id, foto into v_cliente_id, v_foto;

            dbms_lob.writeappend(v_foto, utl_raw.length(v_png), v_png);
            dbms_output.put_line('creado: cliente de demostracion');
    end;

    select foto
      into v_foto
      from cliente
     where cliente_id = v_cliente_id
       for update;

    if dbms_lob.getlength(v_foto) = 0 then
        dbms_lob.writeappend(v_foto, utl_raw.length(v_png), v_png);
    end if;

    commit;
end;
/

