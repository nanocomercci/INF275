USE trazapm;

-- Datos sintéticos de TrazaPM. Versión TP3: GTIN con dígito verificador GS1
-- válido, tercera familia de productos y usuarios para cada rol del prototipo.

INSERT INTO rol (nombre) VALUES
('OPERADOR'), ('SUPERVISOR'), ('PROFESIONAL'), ('ADMINISTRADOR');

INSERT INTO usuario (id_rol, nombre_usuario, hash_clave, nombre_completo) VALUES
((SELECT id_rol FROM rol WHERE nombre='OPERADOR'), 'operador.demo', '$2a$demo$hash-no-productivo', 'Operador de Depósito'),
((SELECT id_rol FROM rol WHERE nombre='SUPERVISOR'), 'supervisor.demo', '$2a$demo$hash-no-productivo', 'Responsable de Trazabilidad'),
((SELECT id_rol FROM rol WHERE nombre='PROFESIONAL'), 'profesional.demo', '$2a$demo$hash-no-productivo', 'Dra. Laura Sintética');

INSERT INTO tipo_producto (nombre) VALUES
('Marcapasos'), ('Stent coronario'), ('Prótesis de cadera');

INSERT INTO producto (id_tipo_producto, gtin, marca, modelo, certificado_anmat, camaras, diametro_mm, liberador_farmaco, material) VALUES
((SELECT id_tipo_producto FROM tipo_producto WHERE nombre='Marcapasos'), '07791234000012', 'CardioDemo', 'Pulse S1', 'PM-1001', 2, NULL, NULL, NULL),
((SELECT id_tipo_producto FROM tipo_producto WHERE nombre='Stent coronario'), '07791234000029', 'VascularDemo', 'Flow 25', 'PM-1002', NULL, 2.50, TRUE, NULL),
((SELECT id_tipo_producto FROM tipo_producto WHERE nombre='Prótesis de cadera'), '07791234000036', 'OrtoDemo', 'Hip Total C', 'PM-1003', NULL, NULL, NULL, 'Titanio');

INSERT INTO ubicacion (nombre, sector) VALUES
('Depósito central', 'Depósito'),
('Farmacia de productos médicos', 'Farmacia'),
('Quirófano 1', 'Quirófano'),
('Cuarentena', 'Depósito restringido');

INSERT INTO agente (razon_social, cuit, gln_cufe, tipo_agente) VALUES
('Proveedor Médico Demo SA', '30710000001', '7791234000012', 'PROVEEDOR'),
('Distribuidora Implantes Demo SRL', '30710000002', '7791234000029', 'DISTRIBUIDOR');

INSERT INTO profesional (nombre, matricula, cuit) VALUES
('Dra. Laura Sintética', 'MP-DEMO-01', '27300000001'),
('Dr. Martín Ejemplo', 'MP-DEMO-02', '20300000002');

INSERT INTO tipo_evento (codigo, nombre, requiere_destino) VALUES
('RECEPCION', 'Recepción', TRUE),
('TRASLADO', 'Traslado interno', TRUE),
('CUARENTENA', 'Puesta en cuarentena', TRUE),
('LEVANTAR_CUARENTENA', 'Levantamiento de cuarentena', TRUE),
('IMPLANTACION', 'Implantación', FALSE),
('DEVOLUCION', 'Devolución', TRUE);

START TRANSACTION;

INSERT INTO unidad_producto (
    id_producto, id_ubicacion_actual, numero_serie, lote, vencimiento, estado_actual
) VALUES (
    (SELECT id_producto FROM producto WHERE gtin='07791234000012'),
    (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
    'SN-DEMO-0001', 'L-2026-01', '2028-12-31', 'DISPONIBLE'
);

INSERT INTO evento (
    id_unidad, id_tipo_evento, id_usuario, id_ubicacion_destino,
    id_agente_origen, documento, motivo, estado_envio
) VALUES (
    LAST_INSERT_ID(),
    (SELECT id_tipo_evento FROM tipo_evento WHERE codigo='RECEPCION'),
    (SELECT id_usuario FROM usuario WHERE nombre_usuario='operador.demo'),
    (SELECT id_ubicacion FROM ubicacion WHERE nombre='Depósito central'),
    (SELECT id_agente FROM agente WHERE cuit='30710000001'),
    'REM-DEMO-001', 'Recepción de datos sintéticos', 'PENDIENTE'
);

COMMIT;
