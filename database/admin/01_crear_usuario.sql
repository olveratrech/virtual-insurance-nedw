-- Autor: Emmanuel Trejo Olvera
-- Proyecto: Virtual Insurance - NEDW
-- Proposito: crear el usuario local y esquema VI_APP en ETOBDA_S1.
-- Ejecucion: requiere una conexion SYSDBA activa.
-- Este script no elimina ni reemplaza usuarios existentes.

WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

SET ECHO OFF
SET FEEDBACK ON
SET HEADING ON
SET PAGESIZE 100
SET LINESIZE 160
SET SERVEROUTPUT ON
SET VERIFY OFF

PROMPT ============================================================
PROMPT Virtual Insurance - creacion del esquema VI_APP
PROMPT ============================================================

ALTER SESSION SET CONTAINER = ETOBDA_S1;

DECLARE
    V_CONTENEDOR VARCHAR2(128);
    V_EXISTE     NUMBER;
BEGIN
    V_CONTENEDOR := SYS_CONTEXT('USERENV', 'CON_NAME');

    IF V_CONTENEDOR <> 'ETOBDA_S1' THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Contenedor incorrecto. Se esperaba ETOBDA_S1 y se obtuvo ' || V_CONTENEDOR
        );
    END IF;

    SELECT COUNT(*)
      INTO V_EXISTE
      FROM DBA_USERS
     WHERE USERNAME = 'VI_APP';

    IF V_EXISTE > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'VI_APP ya existe. El script no lo eliminara ni modificara automaticamente.'
        );
    END IF;
END;
/

ACCEPT VI_APP_PASSWORD CHAR PROMPT 'Contrasena nueva para VI_APP: ' HIDE

CREATE USER VI_APP
    IDENTIFIED BY "&VI_APP_PASSWORD"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS
    CONTAINER = CURRENT;

GRANT CREATE SESSION TO VI_APP;
GRANT CREATE TABLE TO VI_APP;
GRANT CREATE SEQUENCE TO VI_APP;
GRANT CREATE VIEW TO VI_APP;
GRANT CREATE PROCEDURE TO VI_APP;
GRANT CREATE TRIGGER TO VI_APP;

UNDEFINE VI_APP_PASSWORD

PROMPT ============================================================
PROMPT Verificacion del usuario creado
PROMPT ============================================================

COLUMN CONTENEDOR FORMAT A20
COLUMN USERNAME FORMAT A20
COLUMN ACCOUNT_STATUS FORMAT A20
COLUMN DEFAULT_TABLESPACE FORMAT A20

SELECT SYS_CONTEXT('USERENV', 'CON_NAME') AS CONTENEDOR
FROM DUAL;

SELECT USERNAME,
       ACCOUNT_STATUS,
       DEFAULT_TABLESPACE
  FROM DBA_USERS
 WHERE USERNAME = 'VI_APP';

PROMPT Usuario VI_APP creado correctamente.

EXIT SUCCESS
