package org.zalando.fahrschein.http.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class UserAgent {
    private static final String PROPERTIES_FILE = "fahrschein.properties";
    private static final Logger logger = LoggerFactory.getLogger(UserAgent.class);

    private static final Properties fahrscheinProperties = new Properties();

    static {
        try (final InputStream stream =
                     UserAgent.class.getResourceAsStream("/" + PROPERTIES_FILE)) {
            if (stream != null) {
                fahrscheinProperties.load(stream);
            } else {
                logger.warn("Properties file not found: {}", PROPERTIES_FILE);
            }
        } catch (IOException e) {
            logger.warn("Cannot read file: " + PROPERTIES_FILE, e);
        }
    }

    private static final String AGENT_STR_TEMPLATE = "Fahrschein/%s (%s; Java%d)";

    private final String userAgent;

    public UserAgent(Class implementation) {
        this.userAgent = String.format(Locale.ENGLISH, AGENT_STR_TEMPLATE, fahrscheinVersion(), implementation.getSimpleName().replace("RequestFactory", ""), javaVersion());
    }

    public String userAgent() {
        return userAgent;
    }

    static String fahrscheinVersion() {
        return String.valueOf(fahrscheinProperties.get("fahrschein-version"));
    }

    static int javaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }
}
