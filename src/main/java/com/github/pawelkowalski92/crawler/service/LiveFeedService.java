package com.github.pawelkowalski92.crawler.service;

import com.github.pawelkowalski92.crawler.configuration.LiveFeedConfiguration;
import com.github.pawelkowalski92.crawler.model.WebContent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.function.Function;

@Component
public class LiveFeedService {

    private final CrawlerService crawlerService;
    private final LiveFeedConfiguration liveFeedConfiguration;

    private Flux<WebContent<byte[]>> publisher;

    public LiveFeedService(CrawlerService crawlerService, LiveFeedConfiguration liveFeedConfiguration) {
        this.crawlerService = crawlerService;
        this.liveFeedConfiguration = liveFeedConfiguration;
    }

    @PostConstruct
    public void setupPublisher() {
        publisher = crawlerService.streamContent(liveFeedConfiguration.sources(), byte[].class)
                .share();
    }

    public <T> Publisher<WebContent<T>> getPublisher(Function<? super byte[], ? extends T> contentMapper) {
        return publisher.map(webContent -> transformWebContent(webContent, contentMapper));
    }

    @SuppressWarnings("unchecked")
    private <T, R> WebContent<R> transformWebContent(WebContent<T> webContent,
                                                     Function<? super T, ? extends R> contentMapper) {
        if (webContent instanceof WebContent.Failure) {
            return (WebContent<R>) webContent;
        }
        return new WebContent.Success<>(
                webContent.status(),
                webContent.retrievedAt(),
                webContent.content() != null ? contentMapper.apply(webContent.content()) : null
        );
    }

}
