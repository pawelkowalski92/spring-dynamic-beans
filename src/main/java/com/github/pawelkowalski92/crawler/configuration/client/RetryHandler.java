package com.github.pawelkowalski92.crawler.configuration.client;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryHandler implements ExchangeFilterFunction {

    private final Retry retryStrategy;

    public RetryHandler(long retries, Duration backOff) {
        this.retryStrategy = Retry.backoff(retries, backOff)
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .retryWhen(retryStrategy);
    }

}
