package com.github.pawelkowalski92.crawler.provider;

import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties;
import com.github.pawelkowalski92.crawler.configuration.client.ErrorThrower;
import com.github.pawelkowalski92.crawler.configuration.client.RequestLogger;
import com.github.pawelkowalski92.crawler.configuration.client.RetryHandler;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Component
public class CrawlerClientFactory {

    private final ReactorResourceFactory resourceFactory;

    public CrawlerClientFactory(ReactorResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public WebClient createWebClient(CrawlerClientsProperties.ClientDefinition clientDefinition) {
        ClientHttpConnector customHttpConnector = customTimeoutsConnector(
                clientDefinition.connectionTimeout(),
                clientDefinition.readTimeout()
        );
        WebClient.Builder clientBuilder = WebClient.builder()
                .clientConnector(customHttpConnector);
        retryHandler(clientDefinition.retry()).ifPresent(clientBuilder::filter);
        return clientBuilder
                .filter(new RequestLogger())
                .filter(new ErrorThrower())
                .build();
    }

    private ClientHttpConnector customTimeoutsConnector(Duration connectionTimeout, Duration readTimeout) {
        UnaryOperator<HttpClient> connectionTimeoutConfigurer = connectionTimeout != null
                ? httpClient -> httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
                : UnaryOperator.identity();
        UnaryOperator<HttpClient> responseTimeoutConfigurer = readTimeout != null
                ? httpClient -> httpClient.responseTimeout(readTimeout)
                : UnaryOperator.identity();
        return new ReactorClientHttpConnector(
                resourceFactory,
                connectionTimeoutConfigurer.andThen(responseTimeoutConfigurer)
        );
    }

    private Optional<RetryHandler> retryHandler(CrawlerClientsProperties.RetryStrategy retryStrategy) {
        return Optional.ofNullable(retryStrategy)
                .map(retry -> new RetryHandler(retry.maxAttempts(), retry.backOff()));
    }

}
