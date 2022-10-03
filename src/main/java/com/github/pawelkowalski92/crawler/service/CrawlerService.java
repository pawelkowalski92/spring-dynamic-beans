package com.github.pawelkowalski92.crawler.service;

import com.github.pawelkowalski92.crawler.model.CrawlingRequest;
import com.github.pawelkowalski92.crawler.model.WebContent;
import com.github.pawelkowalski92.crawler.provider.ClientProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;

@Component
public class CrawlerService {

    private final Clock clock;
    private final ClientProvider clientProvider;

    public CrawlerService(Clock clock, ClientProvider clientProvider) {
        this.clock = clock;
        this.clientProvider = clientProvider;
    }

    public <T> Flux<WebContent<T>> streamContent(Collection<? extends CrawlingRequest> crawlingRequests,
                                                 Class<? extends T> contentType) {
        return Flux.fromIterable(crawlingRequests)
                .flatMap(streamingRequest -> streamContent(streamingRequest, contentType));
    }

    public <T> Flux<WebContent<T>> streamContent(CrawlingRequest crawlingRequest, Class<? extends T> contentType) {
        Mono<WebContent<T>> contentPublisher = crawlingRequest.clientTag()
                .map(tag -> this.<T>retrieveContent(crawlingRequest.resource(), tag, contentType))
                .orElseGet(() -> retrieveContent(crawlingRequest.resource(), contentType));
        return contentPublisher.delaySubscription(crawlingRequest.frequency())
                .repeat();
    }

    public <T> Mono<WebContent<T>> retrieveContent(URI resource, String clientTag, Class<? extends T> responseType) {
        return clientProvider.findClient(clientTag)
                .map(client -> this.<T>retrieveContent(resource, client, responseType))
                .orElseGet(() -> retrieveContent(resource, responseType));
    }

    public <T> Mono<WebContent<T>> retrieveContent(URI resource, Class<? extends T> responseType) {
        return retrieveContent(resource, clientProvider.getDefaultClient(), responseType);
    }

    private <T> Mono<WebContent<T>> retrieveContent(URI resource, WebClient client, Class<? extends T> responseType) {
        return client.get().uri(resource)
                .exchangeToMono(response -> this.<T>readBody(response, responseType))
                .onErrorResume(WebClientRequestException.class, this::getContentFromRequestException)
                .onErrorResume(WebClientResponseException.class, this::getErrorFromResponseException);
    }

    private <T> Mono<WebContent<T>> readBody(ClientResponse response, Class<? extends T> responseType) {
        return response.<T>bodyToMono(responseType)
                .<WebContent<T>>map(body -> new WebContent.Success<>(response.statusCode(), now(), body))
                .defaultIfEmpty(new WebContent.Success<>(response.statusCode(), now(), null));
    }

    private <T> Mono<WebContent<T>> getContentFromRequestException(WebClientRequestException ex) {
        return Mono.just(
                new WebContent.Failure<>(null, now(), String.format("Unable to connect to: %s; reason: %s",
                        ex.getUri(), ex.getLocalizedMessage()))
        );
    }

    private <T> Mono<WebContent<T>> getErrorFromResponseException(WebClientResponseException ex) {
        return Mono.just(
                new WebContent.Failure<>(ex.getStatusCode(), now(), ex.getResponseBodyAsString(StandardCharsets.UTF_8))
        );
    }

    private Instant now() {
        return Instant.now(clock);
    }

}
