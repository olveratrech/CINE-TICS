package com.penta.cinetics.web;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

/** Explicit allowlist; no URL, schema, or tenant value from a request is interpolated into SQL. */
@Component
public final class ConexionesPorCadena implements AutoCloseable {
    private final Map<String,HikariDataSource> pools = new LinkedHashMap<>();
    public ConexionesPorCadena() {
        try {
            for (String tenant : new String[]{"CINE_TICS","CADENA_DEMO"}) {
                String password = System.getenv(tenant+"_APP_PASSWORD");
                if (password==null || password.isBlank()) throw new IllegalStateException("Falta la credencial de " + tenant);
                var config = new HikariConfig();
                config.setJdbcUrl("jdbc:oracle:thin:@//127.0.0.1:1522/"+tenant);
                config.setUsername("CINE_APP"); config.setPassword(password);
                config.setPoolName("cine-"+tenant); config.setMaximumPoolSize(4); config.setMinimumIdle(0);
                config.setConnectionTimeout(5000); config.setValidationTimeout(2000);
                config.setInitializationFailTimeout(5000);
                pools.put(tenant,new HikariDataSource(config));
            }
        } catch (RuntimeException error) { close(); throw error; }
    }
    public void validar(String tenant) {
        if (!pools.containsKey(tenant)) throw new IllegalArgumentException("Cadena desconocida.");
    }
    public Connection abrir(String tenant) throws SQLException { validar(tenant); return pools.get(tenant).getConnection(); }
    @Override @PreDestroy public void close() { pools.values().forEach(HikariDataSource::close); }
}
