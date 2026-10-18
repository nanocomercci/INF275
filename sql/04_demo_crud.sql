USE trazapm;

-- Demostración controlada: el registro se inserta, consulta y elimina.
START TRANSACTION;

INSERT INTO agente (razon_social, cuit, gln_cufe, tipo_agente)
VALUES ('Agente temporal de prueba', '30719999999', '7799999999999', 'PROVEEDOR');

SELECT id_agente, razon_social, cuit
FROM agente
WHERE cuit = '30719999999';

DELETE FROM agente
WHERE cuit = '30719999999';

SELECT COUNT(*) AS registros_temporales_restantes
FROM agente
WHERE cuit = '30719999999';

COMMIT;
