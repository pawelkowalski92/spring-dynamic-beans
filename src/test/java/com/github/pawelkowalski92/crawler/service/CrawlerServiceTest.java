package com.github.pawelkowalski92.crawler.service;

import com.github.pawelkowalski92.crawler.BaseIntegrationTest;
import com.github.pawelkowalski92.crawler.configuration.SpyWebClientBeanPostProcessor;
import com.github.pawelkowalski92.crawler.configuration.TestClockConfiguration;
import com.github.pawelkowalski92.crawler.model.CrawlingRequest;
import com.github.pawelkowalski92.crawler.model.WebContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.times;

@Import(SpyWebClientBeanPostProcessor.class)
public class CrawlerServiceTest extends BaseIntegrationTest {

    @Autowired
    CrawlerService crawlerService;

    @Test
    public void shouldCrawlForRequestedContentWithDefaultClient() {
        // given
        URI resource = URI.create(baseUri + "/status/201");

        // when
        Mono<WebContent<String>> content = crawlerService.retrieveContent(resource, String.class);

        // then
        StepVerifier.create(content)
                .assertNext(webContent -> {
                    assertThat(webContent.isSuccessful()).isTrue();
                    assertThat(webContent.status()).isSameAs(HttpStatus.CREATED);
                    assertThat(webContent.retrievedAt()).isEqualTo(TestClockConfiguration.FIXED_TIME);
                })
                .expectComplete()
                .verify();

        WebClient usedClient = resolveDefaultClient();
        verify(usedClient).get();
    }

    @Test
    public void shouldCrawlForRequestedContentWithSelectedClient() {
        // given
        URI resource = URI.create(baseUri + "/status/201");
        String clientTag = "fail-fast";

        // when
        Mono<WebContent<String>> content = crawlerService.retrieveContent(resource, clientTag, String.class);

        // then
        StepVerifier.create(content)
                .assertNext(webContent -> {
                    assertThat(webContent.isSuccessful()).isTrue();
                    assertThat(webContent.status()).isSameAs(HttpStatus.CREATED);
                    assertThat(webContent.retrievedAt()).isEqualTo(TestClockConfiguration.FIXED_TIME);
                })
                .expectComplete()
                .verify();

        WebClient usedClient = resolveClient(clientTag);
        verify(usedClient).get();
    }

    static Stream<Arguments> slowClientsOnly() {
        return Stream.of(
                arguments("try-hard", true),
                arguments("balanced", false),
                arguments("fail-fast", false)
        );
    }

    @ParameterizedTest
    @MethodSource("slowClientsOnly")
    public void shouldSucceedCrawlingForLongRespondingContentWithTryHardClientOnly(String clientTag, boolean successful) {
        // given
        URI resource = URI.create(baseUri + "/delay/6");

        // when
        Mono<WebContent<String>> content = crawlerService.retrieveContent(resource, clientTag, String.class);

        // then
        StepVerifier.create(content)
                .assertNext(webContent -> {
                    assertThat(webContent.isSuccessful()).isEqualTo(successful);
                    assertThat(webContent.retrievedAt()).isEqualTo(TestClockConfiguration.FIXED_TIME);
                })
                .expectComplete()
                .verify();

        WebClient usedClient = resolveClient(clientTag);
        verify(usedClient).get();
    }

    @Test
    public void shouldStreamRequestedContentWithDefaultClient() {
        // given
        URI resource = URI.create(baseUri + "/delay/2");
        Duration frequency = Duration.ofSeconds(5L);

        // when
        Flux<WebContent<String>> content = crawlerService.streamContent(
                new CrawlingRequest(resource, Optional.empty(), frequency),
                String.class
        );

        // then
        Consumer<WebContent<?>> contentVerifier = webContent -> {
            assertThat(webContent.isSuccessful()).isTrue();
            assertThat(webContent.status()).isSameAs(HttpStatus.OK);
            assertThat(webContent.retrievedAt()).isEqualTo(TestClockConfiguration.FIXED_TIME);
        };
        StepVerifier.create(content)
                .assertNext(contentVerifier)
                .thenAwait(frequency)
                .assertNext(contentVerifier)
                .thenCancel()
                .verify();

        WebClient usedClient = resolveDefaultClient();
        verify(usedClient).get();
    }

    @Test
    public void shouldStreamRequestedContentWithSelectedClient() {
        // given
        URI resource = URI.create(baseUri + "/delay/2");
        Duration frequency = Duration.ofSeconds(5L);
        String clientTag = "balanced";

        // when
        Flux<WebContent<String>> content = crawlerService.streamContent(
                new CrawlingRequest(resource, Optional.of(clientTag), frequency),
                String.class
        );

        // then
        Consumer<WebContent<?>> contentVerifier = webContent -> {
            assertThat(webContent.isSuccessful()).isTrue();
            assertThat(webContent.status()).isSameAs(HttpStatus.OK);
            assertThat(webContent.retrievedAt()).isEqualTo(TestClockConfiguration.FIXED_TIME);
        };
        StepVerifier.create(content)
                .assertNext(contentVerifier)
                .thenAwait(frequency)
                .assertNext(contentVerifier)
                .thenCancel()
                .verify();

        WebClient usedClient = resolveClient(clientTag);
        verify(usedClient).get();
    }

}
