package com.example.ecommerce.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.Arrays;

public class FlywayApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(FlywayApplicationContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        boolean enabled = environment.getProperty("spring.flyway.enabled", Boolean.class, true);
        if (!enabled) {
            return;
        }

        String url = environment.getProperty("spring.datasource.url");
        if (url == null || url.isBlank()) {
            return;
        }

        DataSource dataSource = buildDataSource(environment, url);
        String locations = environment.getProperty("spring.flyway.locations", "classpath:db/migration");
        String[] locationArray = Arrays.stream(locations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        boolean baselineOnMigrate = environment.getProperty(
                "spring.flyway.baseline-on-migrate", Boolean.class, true);
        boolean validateOnMigrate = environment.getProperty(
                "spring.flyway.validate-on-migrate", Boolean.class, true);
        String table = environment.getProperty("spring.flyway.table", "flyway_schema_history");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locationArray)
                .baselineOnMigrate(baselineOnMigrate)
                .validateOnMigrate(validateOnMigrate)
                .table(table)
                .load();

        try {
            flyway.migrate();
        } catch (FlywayException ex) {
            if (isUnsupportedDatabase(ex)) {
                log.warn("Flyway unsupported database, applying minimal fallback migration: {}", ex.getMessage());
                applyFallbackMigration(dataSource);
                return;
            }
            throw ex;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private DataSource buildDataSource(ConfigurableEnvironment environment, String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);

        String username = environment.getProperty("spring.datasource.username");
        if (username != null) {
            dataSource.setUsername(username);
        }
        String password = environment.getProperty("spring.datasource.password");
        if (password != null) {
            dataSource.setPassword(password);
        }
        String driverClass = environment.getProperty("spring.datasource.driver-class-name");
        if (driverClass != null && !driverClass.isBlank()) {
            dataSource.setDriverClassName(driverClass);
        }

        return dataSource;
    }

    private boolean isUnsupportedDatabase(FlywayException ex) {
        return ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unsupported database");
    }

    private void applyFallbackMigration(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String product = metaData.getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("postgresql")) {
                throw new FlywayException("Unsupported database for fallback migration: " + product);
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE payments ADD COLUMN IF NOT EXISTS authorization_url VARCHAR(500)");
                stmt.execute("ALTER TABLE payments ADD COLUMN IF NOT EXISTS access_code VARCHAR(120)");
            }
        } catch (Exception e) {
            throw new FlywayException("Fallback migration failed", e);
        }
    }
}
