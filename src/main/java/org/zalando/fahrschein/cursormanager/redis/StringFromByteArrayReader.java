package org.zalando.fahrschein.cursormanager.redis;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

final class StringFromByteArrayReader {
    private final DataInputStream dataInputStream;

    StringFromByteArrayReader(final byte[] bytes) {
        dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    public String readNextString() throws IOException {
        final int nextStringLength = dataInputStream.readInt();
        final byte[] bytes = new byte[nextStringLength];
        dataInputStream.read(bytes);
        return new String(bytes, RedisCursorManager.UTF8);
    }

    public StringFromByteArrayReader readString(final Consumer<String> consumer) throws IOException {
        final String s = readNextString();
        consumer.accept(s);
        return this;
    }
}
