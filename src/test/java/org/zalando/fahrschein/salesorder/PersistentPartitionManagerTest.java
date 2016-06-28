package org.zalando.fahrschein.salesorder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.PersistentPartitionManager;

import javax.sql.DataSource;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PersistentPartitionManagerTest.LocalPostgresConfiguration.class)
//@Transactional
public class PersistentPartitionManagerTest extends AbstractPartitionManagerTest {
    private static final Logger LOG = LoggerFactory.getLogger(PersistentPartitionManager.class);

    static class LocalPostgresConfiguration {
        @Bean
        public DataSource dataSource() {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/local_nakadi_cursor_db");
            hikariConfig.setUsername("postgres");
            hikariConfig.setPassword("postgres");
            //hikariConfig.setAutoCommit(false);

            final HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            final String transactionIsolation = hikariDataSource.getTransactionIsolation();

            LOG.info("Transaction isolation level is [{}]", transactionIsolation);

            return hikariDataSource;
        }

        @Bean
        public PartitionManager partitionManager(DataSource dataSource) throws IOException {
            return new PersistentPartitionManager(dataSource);
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PartitionManager partitionManager;

    private int deleteCursors() {
        return new JdbcTemplate(dataSource).update("DELETE FROM nakadi_cursor");
    }

    @Before
    public void setup() {
        deleteCursors();
    }

    @After
    public void teardown() {
        deleteCursors();
    }


    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }
}
