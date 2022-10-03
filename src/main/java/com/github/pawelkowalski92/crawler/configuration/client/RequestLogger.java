package com.github.pawelkowalski92.crawler.configuration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;

public class RequestLogger implements ExchangeFilterFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String requestId = request.logPrefix();
        return Mono.just(request)
                .doOnNext(req -> LOGGER.info("{} Requesting URL: {}", requestId, req.url()))
                .flatMap(next::exchange)
                .doOnSuccess(resp -> LOGGER.info("{} Received HTTP Status code: {}", requestId, resp.statusCode()))
                .doOnError(ex -> LOGGER.error("{} Failed to retrieve content: {}", requestId, ex.getLocalizedMessage()));
    }

}
