package com.github.pawelkowalski92.crawler.configuration.client;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class ErrorThrower implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .flatMap(this::asHttpStatusCodeError);
    }

    private Mono<ClientResponse> asHttpStatusCodeError(ClientResponse response) {
        return response.statusCode().isError()
                ? response.createException().flatMap(Mono::error)
                : Mono.just(response);
    }

}
