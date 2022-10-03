package com.github.pawelkowalski92.crawler.configuration;

import java.time.Duration;
import java.util.List;

public record CrawlerClientsProperties(List<ClientDefinition> definitions) {

    public record ClientDefinition(
            String tag,
            boolean primary,
            Duration connectionTimeout,
            Duration readTimeout,
            RetryStrategy retry
    ) {
    }

    public record RetryStrategy(
            long maxAttempts,
            Duration backOff
    ) {
    }

}
