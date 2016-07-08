package org.zalando.fahrschein.cursormanager.redis;

import java.nio.charset.Charset;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

class Codec {

    public static final char DELIMITER = 0;
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public byte[] serialize(final String... values) {
        return join(values, DELIMITER).getBytes(UTF8);
    }

    public String[] deserialize(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return split(new String(bytes, UTF8), DELIMITER);
    }

}
