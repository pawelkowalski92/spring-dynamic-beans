package com.github.pawelkowalski92.crawler.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "web-crawlers")
@ConstructorBinding
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
            int maxAttempts,
            Duration backOff
    ) {

    }
}
