package org.zalando.fahrschein.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.zalando.fahrschein.BatchHandler;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class LocalPostgresConfiguration {
    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("postgres");
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
    public BatchHandler transactionHandler() {
        return new TransactionalBatchHandler();
    }
}
