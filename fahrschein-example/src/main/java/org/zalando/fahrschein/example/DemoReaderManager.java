package org.zalando.fahrschein.example;

import org.zalando.fahrschein.ReaderManager;
import org.zalando.fahrschein.domain.Subscription;

import java.util.Optional;
import java.util.Set;

  public class DemoReaderManager implements ReaderManager {

    private boolean discontinueReading = false;
    private boolean terminateReader = false;

    @Override
    public boolean discontinueReading(Set<String> eventNames, Optional<Subscription> subscription) {
      return discontinueReading;
    }

    @Override
    public boolean terminateReader(Set<String> eventNames, Optional<Subscription> subscription) {
      return terminateReader;
    }

    void discontinueReading() {
      discontinueReading = true;
    }

    void continueReading() {
      discontinueReading = false;
    }

    void terminateReader() {
      terminateReader = true;
    }
  }