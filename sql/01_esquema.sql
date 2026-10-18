DROP DATABASE IF EXISTS trazapm;
CREATE DATABASE trazapm CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE trazapm;

CREATE TABLE rol (
    id_rol BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_rol BIGINT UNSIGNED NOT NULL,
    nombre_usuario VARCHAR(60) NOT NULL UNIQUE,
    hash_clave VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE tipo_producto (
    id_tipo_producto BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE producto (
    id_producto BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_tipo_producto BIGINT UNSIGNED NOT NULL,
    gtin VARCHAR(14) NOT NULL UNIQUE,
    marca VARCHAR(80) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    certificado_anmat VARCHAR(40),
    -- Atributos propios de cada familia (herencia de tabla única, TP3).
    camaras TINYINT UNSIGNED NULL,
    diametro_mm DECIMAL(4,2) NULL,
    liberador_farmaco BOOLEAN NULL,
    material VARCHAR(60) NULL,
    CONSTRAINT fk_producto_tipo FOREIGN KEY (id_tipo_producto) REFERENCES tipo_producto(id_tipo_producto)
) ENGINE=InnoDB;

CREATE TABLE ubicacion (
    id_ubicacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    sector VARCHAR(80) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE agente (
    id_agente BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    razon_social VARCHAR(120) NOT NULL,
    cuit VARCHAR(11) NOT NULL UNIQUE,
    gln_cufe VARCHAR(30) UNIQUE,
    tipo_agente VARCHAR(40) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE profesional (
    id_profesional BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    matricula VARCHAR(30) NOT NULL UNIQUE,
    cuit VARCHAR(11) NOT NULL UNIQUE,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE unidad_producto (
    id_unidad BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_producto BIGINT UNSIGNED NOT NULL,
    id_ubicacion_actual BIGINT UNSIGNED,
    numero_serie VARCHAR(80) NOT NULL,
    lote VARCHAR(60),
    vencimiento DATE,
    estado_actual ENUM('DISPONIBLE','CUARENTENA','IMPLANTADA','DEVUELTA','DESTRUIDA','EXTRAVIADA') NOT NULL,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_unidad_producto_serie UNIQUE (id_producto, numero_serie),
    CONSTRAINT fk_unidad_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT fk_unidad_ubicacion FOREIGN KEY (id_ubicacion_actual) REFERENCES ubicacion(id_ubicacion)
) ENGINE=InnoDB;

CREATE TABLE tipo_evento (
    id_tipo_evento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    requiere_destino BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE evento (
    id_evento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_unidad BIGINT UNSIGNED NOT NULL,
    id_tipo_evento BIGINT UNSIGNED NOT NULL,
    id_usuario BIGINT UNSIGNED NOT NULL,
    id_ubicacion_origen BIGINT UNSIGNED,
    id_ubicacion_destino BIGINT UNSIGNED,
    id_agente_origen BIGINT UNSIGNED,
    id_agente_destino BIGINT UNSIGNED,
    id_profesional BIGINT UNSIGNED,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    documento VARCHAR(80),
    motivo VARCHAR(255),
    estado_envio ENUM('PENDIENTE','ACEPTADO','RECHAZADO','NO_APLICA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_evento_unidad FOREIGN KEY (id_unidad) REFERENCES unidad_producto(id_unidad),
    CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento(id_tipo_evento),
    CONSTRAINT fk_evento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_evento_ubicacion_origen FOREIGN KEY (id_ubicacion_origen) REFERENCES ubicacion(id_ubicacion),
    CONSTRAINT fk_evento_ubicacion_destino FOREIGN KEY (id_ubicacion_destino) REFERENCES ubicacion(id_ubicacion),
    CONSTRAINT fk_evento_agente_origen FOREIGN KEY (id_agente_origen) REFERENCES agente(id_agente),
    CONSTRAINT fk_evento_agente_destino FOREIGN KEY (id_agente_destino) REFERENCES agente(id_agente),
    CONSTRAINT fk_evento_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional),
    INDEX ix_evento_unidad_fecha (id_unidad, fecha_hora),
    INDEX ix_evento_estado_envio (estado_envio)
) ENGINE=InnoDB;

CREATE TABLE transaccion_externa (
    id_transaccion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_evento BIGINT UNSIGNED NOT NULL,
    codigo_externo VARCHAR(80),
    estado ENUM('PENDIENTE','ACEPTADA','RECHAZADA','ERROR') NOT NULL,
    respuesta VARCHAR(500),
    fecha_intento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_intento INT UNSIGNED NOT NULL DEFAULT 1,
    CONSTRAINT uq_transaccion_intento UNIQUE (id_evento, numero_intento),
    CONSTRAINT fk_transaccion_evento FOREIGN KEY (id_evento) REFERENCES evento(id_evento)
) ENGINE=InnoDB;

CREATE TABLE auditoria (
    id_auditoria BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNSIGNED NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(80) NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    entidad_id BIGINT UNSIGNED,
    resultado VARCHAR(30) NOT NULL,
    detalle VARCHAR(500),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    INDEX ix_auditoria_fecha (fecha_hora)
) ENGINE=InnoDB;
