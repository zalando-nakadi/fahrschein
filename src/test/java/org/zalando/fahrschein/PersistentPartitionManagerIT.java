package org.zalando.fahrschein;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LocalPostgresConfiguration.class)
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class PersistentPartitionManagerIT extends AbstractPartitionManagerTest {

    @Autowired
    private PersistentPartitionManager partitionManager;



    @Override
    protected PartitionManager partitionManager() {
        return partitionManager;
    }
}
