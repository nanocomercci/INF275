USE trazapm;
SET NAMES utf8mb4;

-- TP4: datos sintéticos adicionales para el tablero de stock por familia y estado,
-- las alertas de vencimiento y la exportación a CSV. Ejecutar después de 02 y 06.
-- Los vencimientos se calculan desde la fecha actual para que siempre haya alertas.
-- Puede ejecutarse una sola vez: la restricción UNIQUE (producto, serie) impide duplicar.

START TRANSACTION;

-- SN-MP-0102: disponible
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000012'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Farmacia de productos médicos'), 'SN-MP-0102', 'L-2026-02',
        DATE_ADD(CURDATE(), INTERVAL 150 DAY), 'DISPONIBLE');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0102', 'Recepción de datos sintéticos TP4', 'ACEPTADO');

-- SN-MP-0103: implantada
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000012'), NULL, 'SN-MP-0103', 'L-2026-02',
        DATE_ADD(CURDATE(), INTERVAL 400 DAY), 'IMPLANTADA');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0103', 'Recepción de datos sintéticos TP4', 'ACEPTADO');
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_origen, id_ubicacion_destino,
                    id_agente_destino, id_profesional, fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='IMPLANTACION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='profesional.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), NULL,
        NULL, (SELECT id_profesional FROM profesional WHERE matricula='MP-DEMO-01'), DATE_SUB(NOW(), INTERVAL 3 DAY), 'DOC-TP4-0103', 'Cirugía programada', 'PENDIENTE');

-- SN-ST-0201: disponible
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000029'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), 'SN-ST-0201', 'L-ST-26A',
        DATE_ADD(CURDATE(), INTERVAL 75 DAY), 'DISPONIBLE');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0201', 'Recepción de datos sintéticos TP4', 'ACEPTADO');

-- SN-ST-0202: disponible
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000029'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Quirófano 1'), 'SN-ST-0202', 'L-ST-26A',
        DATE_ADD(CURDATE(), INTERVAL 75 DAY), 'DISPONIBLE');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0202', 'Recepción de datos sintéticos TP4', 'ACEPTADO');

-- SN-ST-0203: cuarentena
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000029'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Cuarentena'), 'SN-ST-0203', 'L-ST-26B',
        DATE_ADD(CURDATE(), INTERVAL -5 DAY), 'CUARENTENA');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0203', 'Recepción de datos sintéticos TP4', 'ACEPTADO');
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_origen, id_ubicacion_destino,
                    id_agente_destino, id_profesional, fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='CUARENTENA'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='supervisor.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Cuarentena'),
        NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DOC-TP4-0203', 'Vencimiento detectado en control', 'PENDIENTE');

-- SN-ST-0204: implantada
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000029'), NULL, 'SN-ST-0204', 'L-ST-26B',
        DATE_ADD(CURDATE(), INTERVAL 300 DAY), 'IMPLANTADA');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0204', 'Recepción de datos sintéticos TP4', 'ACEPTADO');
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_origen, id_ubicacion_destino,
                    id_agente_destino, id_profesional, fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='IMPLANTACION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='profesional.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), NULL,
        NULL, (SELECT id_profesional FROM profesional WHERE matricula='MP-DEMO-02'), DATE_SUB(NOW(), INTERVAL 3 DAY), 'DOC-TP4-0204', 'Cirugía programada', 'PENDIENTE');

-- SN-PC-0301: disponible
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000036'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), 'SN-PC-0301', 'L-PC-2601',
        DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'DISPONIBLE');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0301', 'Recepción de datos sintéticos TP4', 'ACEPTADO');

-- SN-PC-0302: disponible
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000036'), (SELECT id_ubicacion FROM ubicacion WHERE nombre='Farmacia de productos médicos'), 'SN-PC-0302', 'L-PC-2601',
        DATE_ADD(CURDATE(), INTERVAL 500 DAY), 'DISPONIBLE');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0302', 'Recepción de datos sintéticos TP4', 'ACEPTADO');

-- SN-PC-0303: devuelta
INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual)
VALUES ((SELECT id_producto FROM producto WHERE gtin='07791234000036'), NULL, 'SN-PC-0303', 'L-PC-2602',
        DATE_ADD(CURDATE(), INTERVAL 500 DAY), 'DEVUELTA');
SET @u = LAST_INSERT_ID();
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino, id_agente_origen,
                    fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
        (SELECT id_agente FROM agente WHERE cuit='30710000001'),
        DATE_SUB(NOW(), INTERVAL 20 DAY), 'REM-TP4-0303', 'Recepción de datos sintéticos TP4', 'ACEPTADO');
INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_origen, id_ubicacion_destino,
                    id_agente_destino, id_profesional, fecha_hora, documento, motivo, estado_envio)
VALUES (@u, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='DEVOLUCION'),
        (SELECT id_usuario FROM usuario WHERE nombre_usuario='supervisor.demo'),
        (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'), NULL,
        (SELECT id_agente FROM agente WHERE cuit='30710000001'), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DOC-TP4-0303', 'Devolución al proveedor por cambio de medida', 'PENDIENTE');

COMMIT;

-- Verificación: matriz familia × estado (la misma que calcula ReporteStock en Java)
SELECT tp.nombre AS familia, u.estado_actual, COUNT(*) AS unidades
FROM unidad_producto u
JOIN producto p ON p.id_producto = u.id_producto
JOIN tipo_producto tp ON tp.id_tipo_producto = p.id_tipo_producto
GROUP BY tp.nombre, u.estado_actual
ORDER BY tp.nombre, u.estado_actual;
