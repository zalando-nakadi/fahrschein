package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

@FunctionalInterface
public interface ReaderManager {
  boolean continueReading(Set<String> eventNames, Optional<Subscription> subscription);
}
