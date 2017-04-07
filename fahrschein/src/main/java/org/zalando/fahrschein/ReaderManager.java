package org.zalando.fahrschein;

@FunctionalInterface
public interface ReaderManager {
  boolean continueReading();
}
