-- Usuario de base de datos que utiliza la aplicación (ver src/main/resources/trazapm.properties).
-- Ejecutar como root DESPUÉS de 01_esquema.sql.
CREATE USER IF NOT EXISTS 'trazapm'@'localhost' IDENTIFIED BY 'trazapm';
-- Si el usuario ya existía de una ejecución anterior, dejamos su clave alineada con el prototipo.
ALTER USER 'trazapm'@'localhost' IDENTIFIED BY 'trazapm';
GRANT SELECT, INSERT, UPDATE, DELETE ON trazapm.* TO 'trazapm'@'localhost';
FLUSH PRIVILEGES;
