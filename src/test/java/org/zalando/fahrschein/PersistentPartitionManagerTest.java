package org.zalando.fahrschein;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PersistentPartitionManagerTest.LocalPostgresConfiguration.class)
@TransactionConfiguration(defaultRollback = false)
public class PersistentPartitionManagerTest extends AbstractPartitionManagerTest {
    private static final Logger LOG = LoggerFactory.getLogger(PersistentPartitionManagerTest.class);

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class LocalPostgresConfiguration {
        @Bean
        public DataSource dataSource() {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
            hikariConfig.setUsername("postgres");
            hikariConfig.setPassword("postgres");
            hikariConfig.setAutoCommit(false);

            final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            final String transactionIsolation = hikariDataSource.getTransactionIsolation();

            LOG.info("Transaction isolation level is [{}]", transactionIsolation);

            return hikariDataSource;
        }

        @Bean
        public PersistentPartitionManager partitionManager(DataSource dataSource) throws IOException {
            return new PersistentPartitionManager(dataSource);
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Autowired
    private PersistentPartitionManager partitionManager;


    @Before
    public void setup() {
        partitionManager.deleteCursors();
    }

    @After
    public void teardown() {
        partitionManager.deleteCursors();
    }


    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }
}
