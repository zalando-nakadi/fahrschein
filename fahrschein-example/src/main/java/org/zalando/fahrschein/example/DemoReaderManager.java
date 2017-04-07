package org.zalando.fahrschein.example;

import org.zalando.fahrschein.ReaderManager;
import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

public class DemoReaderManager implements ReaderManager {
  
  private boolean discontinueReading = false;

  @Override
  public boolean continueReading(Set<String> eventNames, Optional<Subscription> subscription) {
    return discontinueReading;
  }
  
  void discontinueReading() {
    discontinueReading = true;
  }
  
  void continueReading() {
    discontinueReading = false;
  }
}