-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: cargar roles y un revisor academico de cancelaciones.
-- conexion requerida: vi_app@etobda_s1
-- credenciales: admin.cancelaciones@virtual-insurance.test / Virtual2026!

set serveroutput on

declare
    v_usuario          varchar2(128);
    v_contenedor       varchar2(128);
    v_usuario_id       usuario_interno.usuario_interno_id%type;
    v_hash             varchar2(255) :=
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

    merge into rol_interno destino
    using (
        select 'ADMIN' clave, 'Administracion general de la aplicacion' descripcion from dual
        union all
        select 'CANCELLATION_REVIEWER', 'Revision de solicitudes de cancelacion' from dual
    ) origen
       on (destino.clave = origen.clave)
    when matched then update set destino.descripcion = origen.descripcion
    when not matched then insert (clave, descripcion)
         values (origen.clave, origen.descripcion);

    merge into usuario_interno destino
    using (
        select 'admin.cancelaciones@virtual-insurance.test' email from dual
    ) origen
       on (destino.email = origen.email)
    when matched then update set
        destino.nombre = 'Administrador academico',
        destino.contrasena = v_hash,
        destino.activo = 'S'
    when not matched then insert (
        nombre, email, contrasena, activo, fecha_creacion, ultimo_acceso
    ) values (
        'Administrador academico', origen.email, v_hash, 'S', systimestamp, null
    );

    select usuario_interno_id into v_usuario_id
      from usuario_interno
     where email = 'admin.cancelaciones@virtual-insurance.test';

    merge into usuario_interno_rol destino
    using (
        select v_usuario_id usuario_interno_id, rol_interno_id
          from rol_interno
         where clave in ('ADMIN', 'CANCELLATION_REVIEWER')
    ) origen
       on (
           destino.usuario_interno_id = origen.usuario_interno_id
           and destino.rol_interno_id = origen.rol_interno_id
       )
    when not matched then insert (usuario_interno_id, rol_interno_id)
         values (origen.usuario_interno_id, origen.rol_interno_id);

    commit;
    dbms_output.put_line('roles y usuario interno academico cargados correctamente.');
end;
/
