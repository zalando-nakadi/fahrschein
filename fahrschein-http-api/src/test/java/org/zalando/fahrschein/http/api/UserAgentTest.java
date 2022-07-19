package org.zalando.fahrschein.http.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserAgentTest {

    abstract class DummyRequestFactory implements RequestFactory {}

    @Test
    public void javaVersion() {
        String version = System.getProperty("java.version");
        int javaVersion = UserAgent.javaVersion();
        if (version.startsWith("1.8")) {
            assertEquals(8, javaVersion, "Java 8");
        } else {
            assertTrue(version.startsWith(String.valueOf(javaVersion)));
            assertTrue(javaVersion > 8, "Java " + javaVersion);
        }
    }

    @Test
    public void userAgentString() {
        UserAgent ua = new UserAgent(DummyRequestFactory.class);
        String userAgent = ua.userAgent();
        assertEquals("Fahrschein/0.1.0-SNAPSHOT (Dummy; Java" + UserAgent.javaVersion() + ")", userAgent);
    }

}
