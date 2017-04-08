package org.zalando.fahrschein;

import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

public class DefaultReaderManager implements ReaderManager {
  @Override
  public boolean discontinueReading(Set<String> eventNames, Optional<Subscription> subscription) {
    return false;
  }

  @Override
  public boolean terminateReader(Set<String> eventNames, Optional<Subscription> subscription) {
    return false;
  }
}
