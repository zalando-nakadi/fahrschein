package org.zalando.fahrschein.cursormanager.redis;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

final class StringToByteArrayWriter {
    private final DataOutputStream dataOutputStream;
    private final ByteArrayOutputStream baos;

    StringToByteArrayWriter() {
        baos = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStream(baos);
    }

    public StringToByteArrayWriter write(final String s) throws IOException {
        final byte[] bytes = s.getBytes(RedisCursorManager.UTF8);
        dataOutputStream.writeInt(bytes.length);
        dataOutputStream.write(bytes);
        return this;
    }

    public byte[] serialize() {
        return baos.toByteArray();
    }
}
