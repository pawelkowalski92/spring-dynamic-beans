package com.github.pawelkowalski92.crawler.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@TestConfiguration
public class FailFastClientVerifier {

    private final WebClient failFastClient;

    public FailFastClientVerifier(@Qualifier("fail-fast") WebClient failFastClient) {
        this.failFastClient = failFastClient;
    }

    public WebClient.ResponseSpec verifyClient(URI resource) {
        return failFastClient.get().uri(resource)
                .retrieve();
    }

}
