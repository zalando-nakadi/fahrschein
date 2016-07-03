package org.zalando.fahrschein;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientHttpRequestFactoryHelperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AccessTokenProvider ACCESS_TOKEN_PROVIDER = () -> "TOKEN";

    @Test
    public void shouldWrapWithProblemHandlingAndAuthorization() {
        final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        final ClientHttpRequestFactory wrapped = ClientHttpRequestFactoryHelper.wrap(simpleClientHttpRequestFactory, OBJECT_MAPPER, ACCESS_TOKEN_PROVIDER);
        assertTrue(wrapped instanceof AuthorizedClientHttpRequestFactory);

        final ClientHttpRequestFactory delegate = ((DelegatingClientHttpRequestFactory) wrapped).delegate();
        assertTrue(delegate instanceof ProblemHandlingClientHttpRequestFactory);
    }

    @Test
    public void shouldWrapWithProblemHandling() {
        final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        final AuthorizedClientHttpRequestFactory authorizedClientHttpRequestFactory = new AuthorizedClientHttpRequestFactory(simpleClientHttpRequestFactory, ACCESS_TOKEN_PROVIDER);

        final ClientHttpRequestFactory wrapped = ClientHttpRequestFactoryHelper.wrap(authorizedClientHttpRequestFactory, OBJECT_MAPPER, ACCESS_TOKEN_PROVIDER);
        assertTrue(wrapped instanceof ProblemHandlingClientHttpRequestFactory);
    }

    @Test
    public void shouldWrapWithAuthorization() {
        final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        final ProblemHandlingClientHttpRequestFactory problemHandlingClientHttpRequestFactory = new ProblemHandlingClientHttpRequestFactory(simpleClientHttpRequestFactory, OBJECT_MAPPER);

        final ClientHttpRequestFactory wrapped = ClientHttpRequestFactoryHelper.wrap(problemHandlingClientHttpRequestFactory, OBJECT_MAPPER, ACCESS_TOKEN_PROVIDER);
        assertTrue(wrapped instanceof AuthorizedClientHttpRequestFactory);
    }

    @Test
    public void shouldWrapHttpComponentsClientHttpRequestFactory() {
        final CloseableHttpClient httpClient = HttpClients.custom().build();

        final HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        final ClientHttpRequestFactory wrapped = ClientHttpRequestFactoryHelper.wrap(httpComponentsClientHttpRequestFactory, OBJECT_MAPPER, ACCESS_TOKEN_PROVIDER);
        assertTrue(wrapped instanceof AuthorizedClientHttpRequestFactory);

        final ClientHttpRequestFactory delegate1 = ((DelegatingClientHttpRequestFactory) wrapped).delegate();
        assertTrue(delegate1 instanceof ProblemHandlingClientHttpRequestFactory);

        final ClientHttpRequestFactory delegate2 = ((DelegatingClientHttpRequestFactory) delegate1).delegate();
        assertTrue(delegate2 instanceof CloseableHttpComponentsClientRequestFactory);

        final ClientHttpRequestFactory delegate3 = ((DelegatingClientHttpRequestFactory) delegate2).delegate();
        assertEquals(httpComponentsClientHttpRequestFactory, delegate3);
    }
}
