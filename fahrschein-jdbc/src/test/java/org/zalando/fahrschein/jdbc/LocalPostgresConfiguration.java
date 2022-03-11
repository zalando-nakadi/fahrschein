package org.zalando.fahrschein.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.zalando.fahrschein.BatchHandler;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class LocalPostgresConfiguration {

    public static PostgreSQLContainer db = new PostgreSQLContainer("postgres:13")
            .withDatabaseName("local_nakadi_cursor_db");

    static {
        db.start();
        Flyway flyway = Flyway.configure().locations("fahrschein-db").dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword()).load();
        flyway.migrate();
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(db.getJdbcUrl());
        hikariConfig.setUsername(db.getUsername());
        hikariConfig.setPassword(db.getPassword());
        hikariConfig.setAutoCommit(false);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Qualifier("partition-manager-consumer-1")
    public JdbcPartitionManager partitionManager(DataSource dataSource) throws IOException {
        return new JdbcPartitionManager(dataSource, "test-consumer-1");
    }

    @Bean
    @Qualifier("partition-manager-consumer-2")
    public JdbcPartitionManager partitionManager2(DataSource dataSource) throws IOException {
        return new JdbcPartitionManager(dataSource, "test-consumer-2");
    }

    @Bean
    public JdbcCursorManager cursorManager(DataSource dataSource) {
        return new JdbcCursorManager(dataSource, "test");
    }

    @Bean
    public BatchHandler batchHandler() {
        return new TransactionalBatchHandler();
    }
}
