package org.zalando.fahrschein.redis;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class Codec {

    private static final char DELIMITER_CHAR = 0;
    private static final String DELIMITER_STRING = String.valueOf(DELIMITER_CHAR);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public byte[] serialize(final String... values) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length-1) {
                sb.append(DELIMITER_CHAR);
            }

        }
        return sb.toString().getBytes(UTF8);
    }

    public String[] deserialize(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        final StringTokenizer st = new StringTokenizer(new String(bytes, UTF8), DELIMITER_STRING);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result.toArray(new String[result.size()]);
    }

}
