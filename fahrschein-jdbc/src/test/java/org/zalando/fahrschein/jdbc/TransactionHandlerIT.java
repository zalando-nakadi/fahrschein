package org.zalando.fahrschein.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.zalando.fahrschein.IORunnable;
import org.zalando.fahrschein.BatchHandler;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LocalPostgresConfiguration.class)
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class TransactionHandlerIT {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionHandlerIT.class);

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private BatchHandler batchHandler;

    @Test
    public void shouldRollbackOnCursorManagerException() throws IOException {

        new TransactionTemplate(transactionManager).execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus transactionStatus) {
                assertFalse("Transaction should be active", transactionStatus.isRollbackOnly());

                try {
                    batchHandler.processBatch(new IORunnable() {
                        @Override
                        public void run() throws IOException {
                            throw new IOException("commit failed");
                        }
                    });
                } catch (IOException e) {
                    LOG.info("Ignoring [{}] with message [{}]", e.getClass().getName(), e.getMessage());
                }

                assertTrue("Transaction should be marked as rollback only", transactionStatus.isRollbackOnly());

                return null;
            }
        });

    }
}
