package com.trazapm.dao.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lee los parámetros de conexión y abre la conexión JDBC con MySQL.
 * Busca primero un archivo trazapm.properties junto al programa (para que
 * cada puesto configure su servidor) y, si no existe, usa el incluido en
 * src/main/resources.
 */
public final class ConexionMySQL {

    private ConexionMySQL() {
    }

    public static Connection abrir() throws SQLException {
        Properties p = leerConfiguracion();
        String url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/trazapm");
        DriverManager.setLoginTimeout(3);
        return DriverManager.getConnection(url, p.getProperty("db.usuario", "root"), p.getProperty("db.clave", ""));
    }

    static Properties leerConfiguracion() {
        Properties p = new Properties();
        Path externo = Path.of("trazapm.properties");
        try (InputStream in = Files.exists(externo)
                ? Files.newInputStream(externo)
                : ConexionMySQL.class.getResourceAsStream("/trazapm.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            // Sin archivo de configuración se usan los valores por defecto.
        }
        return p;
    }
}
