package org.zalando.fahrschein;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {
    InputStream open(ConnectionParameters connectionParameters) throws IOException, InterruptedException;
}
