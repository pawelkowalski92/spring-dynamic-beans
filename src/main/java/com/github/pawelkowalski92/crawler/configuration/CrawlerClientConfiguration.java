package com.github.pawelkowalski92.crawler.configuration;

import com.github.pawelkowalski92.crawler.configuration.client.ErrorThrower;
import com.github.pawelkowalski92.crawler.configuration.client.RequestLogger;
import com.github.pawelkowalski92.crawler.configuration.client.RetryHandler;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.function.UnaryOperator;

@Configuration
public class CrawlerClientConfiguration {

    @Bean("fail-fast")
    public WebClient failFastClient(ReactorResourceFactory resourceFactory,
                                    @Value("${web-crawlers.definitions[0].connection-timeout}") Duration connectionTimeout,
                                    @Value("${web-crawlers.definitions[0].read-timeout}") Duration readTimeout,
                                    @Value("${web-crawlers.definitions[0].retry.max-attempts}") Long retries,
                                    @Value("${web-crawlers.definitions[0].retry.back-off}") Duration retryBackOff) {
        return createWebClient(resourceFactory, connectionTimeout, readTimeout, retries, retryBackOff);
    }

    @Bean("try-hard")
    public WebClient tryHardClient(ReactorResourceFactory resourceFactory,
                                   @Value("${web-crawlers.definitions[1].connection-timeout}") Duration connectionTimeout,
                                   @Value("${web-crawlers.definitions[1].read-timeout}") Duration readTimeout,
                                   @Value("${web-crawlers.definitions[1].retry.max-attempts}") Long retries,
                                   @Value("${web-crawlers.definitions[1].retry.back-off}") Duration retryBackOff) {
        return createWebClient(resourceFactory, connectionTimeout, readTimeout, retries, retryBackOff);
    }

    @Bean("balanced")
    @Primary
    public WebClient balancedClient(ReactorResourceFactory resourceFactory,
                                    @Value("${web-crawlers.definitions[2].connection-timeout}") Duration connectionTimeout,
                                    @Value("${web-crawlers.definitions[2].read-timeout}") Duration readTimeout,
                                    @Value("${web-crawlers.definitions[2].retry.max-attempts}") Long retries,
                                    @Value("${web-crawlers.definitions[2].retry.back-off}") Duration retryBackOff) {
        return createWebClient(resourceFactory, connectionTimeout, readTimeout, retries, retryBackOff);
    }

    private WebClient createWebClient(ReactorResourceFactory resourceFactory,
                                      Duration connectionTimeout,
                                      Duration responseTimeout,
                                      Long retries,
                                      Duration retryBackOff) {
        UnaryOperator<HttpClient> connectionTimeoutConfigurer = connectionTimeout != null
                ? httpClient -> httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
                : UnaryOperator.identity();
        UnaryOperator<HttpClient> responseTimeoutConfigurer = responseTimeout != null
                ? httpClient -> httpClient.responseTimeout(responseTimeout)
                : UnaryOperator.identity();
        ClientHttpConnector customHttpConnector = new ReactorClientHttpConnector(
                resourceFactory,
                connectionTimeoutConfigurer.andThen(responseTimeoutConfigurer)
        );
        WebClient.Builder clientBuilder = WebClient.builder()
                .clientConnector(customHttpConnector);
        if (retries != null && retryBackOff != null) {
            clientBuilder.filter(new RetryHandler(retries, retryBackOff));
        }
        return clientBuilder
                .filter(new RequestLogger())
                .filter(new ErrorThrower())
                .build();
    }

}
