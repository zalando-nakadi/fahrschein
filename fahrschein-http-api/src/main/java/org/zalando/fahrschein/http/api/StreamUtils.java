package org.zalando.fahrschein.http.api;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    /**
     * The default buffer size used when copying bytes.
     */
    public static final int BUFFER_SIZE = 4096;

    /**
     * Drain the remaining content of the given InputStream.
     * <p>Leaves the InputStream open when done.
     * @param in the InputStream to drain
     * @return the number of bytes read
     * @throws IOException in case of I/O errors
     */
    public static int drain(InputStream in) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        int byteCount = 0;
        while ((bytesRead = in.read(buffer)) != -1) {
            byteCount += bytesRead;
        }
        return byteCount;
    }

}
