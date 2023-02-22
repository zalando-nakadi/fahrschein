package org.zalando.fahrschein;

import lombok.AllArgsConstructor;
import org.zalando.fahrschein.AccessTokenProvider;
import org.zalando.fahrschein.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default {@link AccessTokenProvider access token provider} built for Zalando's Platform IAM which provides
 * OAuth2 tokens as files in a mounted directory. Each token is represented as a set of two individual files:
 *
 * <dl>
 *     <dt>{name}-token-type</dt>
 *     <dd>Contains the token type, e.g. Bearer.</dd>
 *
 *     <dt>{name}-token-secret</dt>
 *     <dd>Contains the actual secret, e.g. a Json Web Token (JWT)</dd>
 * </dl>
 *
 * @see <a href="https://kubernetes-on-aws.readthedocs.io/en/latest/user-guide/zalando-iam.html">Zalando Platform IAM Integration</a>
 */
@AllArgsConstructor
public class PlatformAccessTokenProvider implements AccessTokenProvider {

    private static final String TOKEN_SECRET_SUFFIX = "-token-secret";

    private static final String DEFAULT_CREDENTIALS_DIRECTORY = "/meta/credentials";

    private static final String DEFAULT_APPLICATION_NAME = "nakadi";

    private final String directory;

    private final String name;

    public PlatformAccessTokenProvider() {
        this(DEFAULT_CREDENTIALS_DIRECTORY, DEFAULT_APPLICATION_NAME);
    }

    public PlatformAccessTokenProvider(final String name) {
        this(DEFAULT_CREDENTIALS_DIRECTORY, name);
    }

    @Override
    public String getAccessToken() throws IOException {
        final Path filePath = Paths.get(this.directory, name + TOKEN_SECRET_SUFFIX);
        final List<String> lines = Files.readAllLines(filePath, UTF_8);
        Preconditions.checkArgument(!lines.isEmpty(), "Secret file cannot be empty");
        return Files.readAllLines(filePath, UTF_8).get(0);
    }
}
