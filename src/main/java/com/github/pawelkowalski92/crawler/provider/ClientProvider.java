package com.github.pawelkowalski92.crawler.provider;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

public interface ClientProvider {

    Optional<WebClient> findClient(String tag);

    WebClient getDefaultClient();

}
