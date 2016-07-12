package org.zalando.fahrschein.domain;

import org.junit.Assert;
import org.junit.Test;

public class PartitionTest {
    @Test
    public void newestOffsetShouldBeAvailable() {
        Assert.assertTrue(new Partition("0", "0", "10").isAvailable("10"));
    }

    @Test
    public void oldestOffsetShouldBeAvailable() {
        Assert.assertTrue(new Partition("0", "10", "20").isAvailable("10"));
    }

    @Test
    public void olderThanOldestOffsetShouldNotBeAvailable() {
        Assert.assertFalse(new Partition("0", "10", "20").isAvailable("9"));
    }
}
