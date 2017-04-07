package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;

@FunctionalInterface
public interface ReaderManager {
  boolean continueReading(Optional<Subscription> subscription);
}
