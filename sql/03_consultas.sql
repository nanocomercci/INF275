USE trazapm;

-- Q1: estado y ubicación actual de una unidad.
SELECT p.gtin, u.numero_serie, u.lote, u.vencimiento,
       u.estado_actual, ub.nombre AS ubicacion_actual
FROM unidad_producto u
JOIN producto p ON p.id_producto = u.id_producto
LEFT JOIN ubicacion ub ON ub.id_ubicacion = u.id_ubicacion_actual
WHERE p.gtin = '07791234000012' AND u.numero_serie = 'SN-DEMO-0001';

-- Q2: historial cronológico completo.
SELECT e.id_evento, te.codigo AS evento, e.fecha_hora,
       uo.nombre AS origen, ud.nombre AS destino,
       e.documento, e.estado_envio, us.nombre_usuario
FROM evento e
JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento
JOIN usuario us ON us.id_usuario = e.id_usuario
LEFT JOIN ubicacion uo ON uo.id_ubicacion = e.id_ubicacion_origen
LEFT JOIN ubicacion ud ON ud.id_ubicacion = e.id_ubicacion_destino
WHERE e.id_unidad = (
    SELECT u.id_unidad
    FROM unidad_producto u
    JOIN producto p ON p.id_producto = u.id_producto
    WHERE p.gtin = '07791234000012' AND u.numero_serie = 'SN-DEMO-0001'
)
ORDER BY e.fecha_hora, e.id_evento;

-- Q3: eventos pendientes para el adaptador simulado.
SELECT e.id_evento, te.codigo, e.fecha_hora, p.gtin, u.numero_serie
FROM evento e
JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento
JOIN unidad_producto u ON u.id_unidad = e.id_unidad
JOIN producto p ON p.id_producto = u.id_producto
WHERE e.estado_envio = 'PENDIENTE'
ORDER BY e.fecha_hora;

-- Q4: unidades próximas a vencer dentro de 180 días.
SELECT p.gtin, u.numero_serie, u.vencimiento, u.estado_actual
FROM unidad_producto u
JOIN producto p ON p.id_producto = u.id_producto
WHERE u.vencimiento BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY)
  AND u.estado_actual IN ('DISPONIBLE','CUARENTENA')
ORDER BY u.vencimiento;
