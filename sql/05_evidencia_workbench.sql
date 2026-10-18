-- TrazaPM - consultas preparadas para registrar evidencia en MySQL Workbench 8.0
-- Ejecutar previamente 01_esquema.sql y 02_datos_prueba.sql.

USE trazapm;

-- Evidencia 1: versión, esquema y tablas creadas.
SELECT VERSION() AS version_mysql, DATABASE() AS esquema_activo;
SHOW TABLES;

-- Evidencia 2: estado actual de la unidad.
SELECT
    p.gtin,
    up.numero_serie,
    up.lote,
    up.vencimiento,
    up.estado_actual,
    u.nombre AS ubicacion_actual
FROM unidad_producto AS up
INNER JOIN producto AS p ON p.id_producto = up.id_producto
INNER JOIN ubicacion AS u ON u.id_ubicacion = up.id_ubicacion_actual
WHERE p.gtin = '07791234000012'
  AND up.numero_serie = 'SN-DEMO-0001';

-- Evidencia 3: historial cronológico.
SELECT
    e.id_evento,
    te.nombre AS evento,
    e.fecha_hora,
    origen.nombre AS origen,
    destino.nombre AS destino,
    e.documento,
    e.estado_envio,
    us.nombre_usuario
FROM evento AS e
INNER JOIN tipo_evento AS te ON te.id_tipo_evento = e.id_tipo_evento
INNER JOIN usuario AS us ON us.id_usuario = e.id_usuario
LEFT JOIN ubicacion AS origen ON origen.id_ubicacion = e.id_ubicacion_origen
LEFT JOIN ubicacion AS destino ON destino.id_ubicacion = e.id_ubicacion_destino
WHERE e.id_unidad = 1
ORDER BY e.fecha_hora, e.id_evento;

-- Evidencia 4: demostración controlada de INSERT y DELETE.
START TRANSACTION;
INSERT INTO agente (razon_social, cuit, tipo_agente)
VALUES ('Agente temporal de prueba', '30719999999', 'PROVEEDOR');
SELECT id_agente, razon_social, cuit
FROM agente
WHERE cuit = '30719999999';
DELETE FROM agente WHERE cuit = '30719999999';
SELECT COUNT(*) AS registros_temporales_restantes
FROM agente
WHERE cuit = '30719999999';
COMMIT;
