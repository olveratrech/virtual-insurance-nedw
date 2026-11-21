-- autor: Emmanuel Trejo Olvera
-- proyecto: Virtual Insurance - NEDW
-- proposito: cargar instrumentos exclusivamente ficticios para la pasarela academica.
-- conexion requerida: vi_app@etobda_s1

merge into instrumento_pago_prueba destino
using (
    select 'tok_academic_funded' token,
           lower(rawtohex(standard_hash('4242424242421111', 'SHA256'))) numero_hash,
           '1111' ultimos_cuatro, 50000 saldo_disponible, 'ACTIVE' estatus,
           2030 anio_expiracion, 12 mes_expiracion,
           cast(null as varchar2(40)) respuesta_forzada
      from dual
    union all
    select 'tok_academic_insufficient', lower(rawtohex(standard_hash('4000000000002222', 'SHA256'))),
           '2222', 100, 'ACTIVE', 2030, 12, null from dual
    union all
    select 'tok_academic_blocked', lower(rawtohex(standard_hash('4000000000003333', 'SHA256'))),
           '3333', 50000, 'BLOCKED', 2030, 12, null from dual
    union all
    select 'tok_academic_expired', lower(rawtohex(standard_hash('4000000000004444', 'SHA256'))),
           '4444', 50000, 'ACTIVE', 2025, 1, null from dual
    union all
    select 'tok_academic_declined', lower(rawtohex(standard_hash('4000000000005555', 'SHA256'))),
           '5555', 50000, 'ACTIVE', 2030, 12, 'PROCESSOR_DECLINED' from dual
    union all
    select 'tok_academic_exact', lower(rawtohex(standard_hash('4242424242426666', 'SHA256'))),
           '6666', 1040, 'ACTIVE', 2030, 12, null from dual
    union all
    select 'tok_mastercard_funded', lower(rawtohex(standard_hash('5105105105105100', 'SHA256'))),
           '5100', 50000, 'ACTIVE', 2030, 12, null from dual
    union all
    select 'tok_amex_funded', lower(rawtohex(standard_hash('371449635398431', 'SHA256'))),
           '8431', 50000, 'ACTIVE', 2030, 12, null from dual
) fuente
on (destino.token = fuente.token)
when matched then update set
    destino.numero_hash = fuente.numero_hash,
    destino.ultimos_cuatro = fuente.ultimos_cuatro,
    destino.saldo_disponible = fuente.saldo_disponible,
    destino.estatus = fuente.estatus,
    destino.anio_expiracion = fuente.anio_expiracion,
    destino.mes_expiracion = fuente.mes_expiracion,
    destino.respuesta_forzada = fuente.respuesta_forzada
when not matched then insert (
    token, numero_hash, ultimos_cuatro, saldo_disponible, estatus,
    anio_expiracion, mes_expiracion, respuesta_forzada, version
) values (
    fuente.token, fuente.numero_hash, fuente.ultimos_cuatro, fuente.saldo_disponible,
    fuente.estatus, fuente.anio_expiracion, fuente.mes_expiracion,
    fuente.respuesta_forzada, 0
);

commit;

