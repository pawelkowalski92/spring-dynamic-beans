package com.github.pawelkowalski92.crawler.api;

import com.github.pawelkowalski92.crawler.model.CrawlingRequest;
import com.github.pawelkowalski92.crawler.model.WebContent;
import com.github.pawelkowalski92.crawler.service.LiveFeedService;
import com.github.pawelkowalski92.crawler.service.CrawlerService;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final LiveFeedService crawlerPublisher;

    public CrawlerController(CrawlerService crawlerService, LiveFeedService crawlerPublisher) {
        this.crawlerService = crawlerService;
        this.crawlerPublisher = crawlerPublisher;
    }

    @GetMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<WebContent<String>> previewPage(@RequestParam("page") URI page,
                                                @RequestParam(value = "client-tag", required = false) Optional<String> clientTag) {
        return clientTag.map(client -> crawlerService.retrieveContent(page, client, String.class))
                .orElseGet(() -> crawlerService.retrieveContent(page, String.class));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WebContent<String>> streamPage(@RequestParam("page") URI page,
                                               @RequestParam(value = "frequency", required = false) Optional<Duration> frequency,
                                               @RequestParam(value = "client-tag", required = false) Optional<String> clientTag) {
        return crawlerService.streamContent(
                new CrawlingRequest(page, clientTag, frequency.orElseGet(() -> Duration.ofSeconds(1L))),
                String.class
        );
    }

    @GetMapping(value = "/live-feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Publisher<WebContent<String>> liveFeed() {
        return crawlerPublisher.getPublisher(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

}
