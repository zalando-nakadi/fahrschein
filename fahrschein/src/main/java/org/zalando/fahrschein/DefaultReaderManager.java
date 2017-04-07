package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

public class DefaultReaderManager implements ReaderManager {
  @Override
  public boolean continueReading(Set<String> eventNames, Optional<Subscription> subscription) {
    return true;
  }
}
