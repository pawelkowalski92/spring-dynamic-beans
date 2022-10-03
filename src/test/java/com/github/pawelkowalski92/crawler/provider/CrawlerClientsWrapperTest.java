package com.github.pawelkowalski92.crawler.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CrawlerClientsWrapperTest {

    @Autowired
    CrawlerClientsWrapper clientProvider;

    @Autowired
    @Qualifier("balanced")
    WebClient defaultClient;

    @ParameterizedTest
    @ValueSource(strings = {"try-hard", "balanced", "fail-fast"})
    public void shouldReturnWebClientForGivenTag(String tag) {
        // given tag

        // when
        Optional<WebClient> clientOptional = clientProvider.findClient(tag);

        // then
        assertThat(clientOptional).isPresent();
    }

    @Test
    public void shouldReturnPrimaryClientAsDefault() {
        // given no tag

        // when
        WebClient client = clientProvider.getDefaultClient();

        // then
        assertThat(client).isSameAs(defaultClient);
    }

    @Test
    public void shouldReturnEmptyForUnknownTag() {
        // given
        String tag = "unknown";

        // when
        Optional<WebClient> clientOptional = clientProvider.findClient(tag);

        // then
        assertThat(clientOptional).isEmpty();
    }

}
