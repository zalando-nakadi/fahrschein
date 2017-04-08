package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

public interface ReaderManager {
  boolean discontinueReading(Set<String> eventNames, Optional<Subscription> subscription);

  boolean terminateReader(Set<String> eventNames, Optional<Subscription> subscription);
}
