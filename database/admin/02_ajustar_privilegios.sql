-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: completar privilegios del esquema vi_app para columnas identity.
-- ejecucion: requiere una conexion sysdba activa.
-- repetible: grant puede ejecutarse nuevamente sin duplicar privilegios.

set serveroutput on

alter session set container = etobda_s1;

declare
    v_contenedor varchar2(128);
    v_existe     number;
begin
    v_contenedor := sys_context('USERENV', 'CON_NAME');

    if v_contenedor <> 'ETOBDA_S1' then
        raise_application_error(
            -20001,
            'contenedor incorrecto. se esperaba ETOBDA_S1 y se obtuvo ' || v_contenedor
        );
    end if;

    select count(*)
      into v_existe
      from dba_users
     where username = 'VI_APP';

    if v_existe = 0 then
        raise_application_error(-20002, 'vi_app no existe en etobda_s1.');
    end if;
end;
/

grant create sequence to vi_app;

column privilege format a30

select privilege
  from dba_sys_privs
 where grantee = 'VI_APP'
   and privilege in ('CREATE TABLE', 'CREATE SEQUENCE')
 order by privilege;

prompt privilegio create sequence verificado para vi_app.

