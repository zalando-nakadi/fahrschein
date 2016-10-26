package org.zalando.fahrschein.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.fahrschein.PartitionManager;
import org.zalando.fahrschein.domain.Lock;
import org.zalando.fahrschein.domain.Partition;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class JdbcPartitionManager implements PartitionManager {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcPartitionManager.class);

    static class LockedPartition {
        final String consumerName;
        final String eventName;
        final String partition;

        public LockedPartition(String consumerName, String eventName, String partition) {
            this.consumerName = consumerName;
            this.eventName = eventName;
            this.partition = partition;
        }
    }

    private final JdbcTemplate template;
    private final String consumerName;
    private final String schemaPrefix;

    public JdbcPartitionManager(JdbcTemplate template, String consumerName, String schema) {
        checkState(schema != null && !schema.isEmpty(), "Schema name should not be null or empty");
        this.template = template;
        this.consumerName = consumerName;
        this.schemaPrefix = schema + ".";
    }

    public JdbcPartitionManager(JdbcTemplate template, String consumerName) {
        this.template = template;
        this.consumerName = consumerName;
        this.schemaPrefix = "";
    }

    public JdbcPartitionManager(DataSource dataSource, String consumerName, String schema) {
        this(new JdbcTemplate(dataSource), consumerName, schema);
    }

    public JdbcPartitionManager(DataSource dataSource, String consumerName) {
        this(new JdbcTemplate(dataSource), consumerName);
    }

    private LockedPartition getLockedPartition(ResultSet rs, int idx) throws SQLException {
        return new LockedPartition(rs.getString(1), rs.getString(2), rs.getString(3));
    }

    private String formatPartitionIds(List<Partition> partitions) {
        return partitions.stream().map(Partition::getPartition).collect(joining(",", "{", "}"));
    }

    @Override
    @Transactional
    public Optional<Lock> lockPartitions(String eventName, List<Partition> partitions, String lockedBy) {
        final String sql = String.format("SELECT * FROM %snakadi_partition_lock(?, ?, ?::text[], ?)", schemaPrefix);

        final String partitionIds = formatPartitionIds(partitions);

        final List<LockedPartition> lockedPartitions = template.query(sql,
                new Object[]{consumerName, eventName, partitionIds, lockedBy},
                this::getLockedPartition);

        if (lockedPartitions.isEmpty()) {
            return Optional.<Lock>empty();
        } else {
            final Map<String, Partition> partitionsById = partitions.stream().collect(toMap(Partition::getPartition, p -> p));
            final List<Partition> collect = lockedPartitions.stream().map(lp -> partitionsById.get(lp.partition)).collect(toList());
            return Optional.of(new Lock(eventName, lockedBy, collect));
        }
    }

    @Override
    @Transactional
    public void unlockPartitions(Lock lock) {
        final String sql = String.format("SELECT * FROM %snakadi_partition_unlock(?, ?, ?::text[], ?)", schemaPrefix);

        final String partitionIds = formatPartitionIds(lock.getPartitions());

        final List<LockedPartition> unlockedPartitions = template.query(sql,
                new Object[]{consumerName, lock.getEventName(), partitionIds, lock.getLockedBy()},
                this::getLockedPartition);

        if (unlockedPartitions.isEmpty()) {
            throw new IllegalStateException("Could not unlock [" + lock.getEventName() + "] by [" + lock.getLockedBy() + "]");
        }
    }
}
