package com.github.pawelkowalski92.crawler.api;

import com.github.pawelkowalski92.crawler.model.CrawlingRequest;
import com.github.pawelkowalski92.crawler.model.WebContent;
import com.github.pawelkowalski92.crawler.service.CrawlerService;
import com.github.pawelkowalski92.crawler.service.LiveFeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@WebFluxTest
public class CrawlerControllerTest {

    private static final ParameterizedTypeReference<WebContent.Success<String>> STRING_SUCCESS_TYPE = new ParameterizedTypeReference<>() {
    };

    @Autowired
    WebTestClient testClient;

    @MockBean
    CrawlerService crawlerService;

    @MockBean
    LiveFeedService liveFeedService;

    @Test
    public void shouldPreviewResource() {
        // given
        String resource = "https://test.com";
        given(crawlerService.retrieveContent(eq(URI.create(resource)), eq(String.class)))
                .willReturn(Mono.just(
                        new WebContent.Success<>(HttpStatus.OK, Instant.now(), "test")
                ));

        // when
        testClient.get()
                .uri(builder -> builder.path("/crawler/preview")
                        .queryParam("page", resource)
                        .queryParam("client-tag", "fail-fast")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        // then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(STRING_SUCCESS_TYPE)
                .value(webContent -> {
                    assertThat(webContent.isSuccessful()).isTrue();
                    assertThat(webContent.content()).isEqualTo("test");
                });
    }

    @Test
    public void shouldStreamResource() {
        // given
        String resource = "https://test.com";
        CrawlingRequest streamingRequest = new CrawlingRequest(
                URI.create(resource),
                Optional.of("fail-fast"),
                Duration.ofSeconds(1L)
        );
        given(crawlerService.streamContent(eq(streamingRequest), eq(String.class)))
                .willReturn(Flux.just(
                        new WebContent.Success<>(HttpStatus.OK, Instant.now(), "test"),
                        new WebContent.Success<>(HttpStatus.OK, Instant.now(), "test2")
                ));

        // when
        testClient.get()
                .uri(builder -> builder.path("/crawler/stream")
                        .queryParam("page", resource)
                        .queryParam("client-tag", "fail-fast")
                        .queryParam("frequency", Duration.ofSeconds(1L))
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
        // then
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(STRING_SUCCESS_TYPE)
                .consumeWith(result -> StepVerifier.create(result.getResponseBody())
                        .assertNext(webContent -> {
                            assertThat(webContent.isSuccessful()).isTrue();
                            assertThat(webContent.content()).isEqualTo("test");
                        })
                        .assertNext(webContent -> {
                            assertThat(webContent.isSuccessful()).isTrue();
                            assertThat(webContent.content()).isEqualTo("test2");
                        })
                        .expectComplete()
                        .verify());
    }

    @Test
    public void shouldProvideLiveFeed() {
        // given
        given(liveFeedService.getPublisher(any()))
                .willReturn(Flux.just(
                        new WebContent.Success<>(HttpStatus.OK, Instant.now(), "test"),
                        new WebContent.Success<>(HttpStatus.OK, Instant.now(), "test2")
                ));

        // when
        testClient.get()
                .uri("/crawler/live-feed")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
        // then
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(STRING_SUCCESS_TYPE)
                .consumeWith(result -> StepVerifier.create(result.getResponseBody())
                        .assertNext(webContent -> {
                            assertThat(webContent.isSuccessful()).isTrue();
                            assertThat(webContent.content()).isEqualTo("test");
                        })
                        .assertNext(webContent -> {
                            assertThat(webContent.isSuccessful()).isTrue();
                            assertThat(webContent.content()).isEqualTo("test2");
                        })
                        .expectComplete()
                        .verify());
    }

}
