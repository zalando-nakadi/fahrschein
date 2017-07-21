package org.zalando.fahrschein.jdbc;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.test.AbstractPartitionManagerTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LocalPostgresConfiguration.class)
@Rollback
@Transactional
public class JdbcPartitionManagerIT extends AbstractPartitionManagerTest {

    @Autowired
    @Qualifier("partition-manager-consumer-1")
    private JdbcPartitionManager partitionManager;

    @Autowired
    @Qualifier("partition-manager-consumer-2")
    private JdbcPartitionManager partitionManager2;


    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }

    @Override
    protected PartitionManager partitionManagerForAnotherConsumer() {
        return partitionManager2;
    }
}
