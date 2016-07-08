package org.zalando.fahrschein.redis;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.nio.charset.Charset;

import static com.google.common.collect.Iterables.toArray;

class Codec {

    public static final char DELIMITER = 0;
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final Joiner JOINER = Joiner.on(DELIMITER);
    public static final Splitter SPLITTER = Splitter.on(DELIMITER);

    public byte[] serialize(final String... values) {
        return JOINER.join(values).getBytes(UTF8);
    }

    public String[] deserialize(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return toArray(SPLITTER.split(new String(bytes, UTF8)), String.class);
    }

}
