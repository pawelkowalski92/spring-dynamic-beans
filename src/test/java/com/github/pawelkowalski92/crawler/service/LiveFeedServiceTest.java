package com.github.pawelkowalski92.crawler.service;

import com.github.pawelkowalski92.crawler.BaseIntegrationTest;
import com.github.pawelkowalski92.crawler.model.WebContent;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LiveFeedServiceTest extends BaseIntegrationTest {

    @Autowired
    LiveFeedService liveFeedService;

    @Test
    public void shouldProperlyReceiveAllFeeds() {
        // given
        Publisher<WebContent<String>> liveFeedPublisher = liveFeedService.getPublisher(
                bytes -> new String(bytes, StandardCharsets.UTF_8)
        );

        // then
        StepVerifier.create(liveFeedPublisher)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(eventsReceivedBelow(10))
                .expectRecordedMatches(webContents -> webContents.stream().allMatch(WebContent::isSuccessful))
                .thenCancel()
                .verify();
    }

}
