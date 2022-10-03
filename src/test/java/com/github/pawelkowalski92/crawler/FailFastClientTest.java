package com.github.pawelkowalski92.crawler;

import com.github.pawelkowalski92.crawler.configuration.FailFastClientVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(FailFastClientVerifier.class)
public class FailFastClientTest extends BaseIntegrationTest {

    @Autowired
    FailFastClientVerifier failFastClientVerifier;

    @Test
    public void contextLoads() {
    }

    @Test
    public void shouldRetrieveContent() {
        // given
        URI resource = URI.create(baseUri + "/xml");

        // when
        Mono<ResponseEntity<String>> response = failFastClientVerifier.verifyClient(resource)
                .toEntity(String.class);

        // then
        StepVerifier.create(response)
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isSameAs(HttpStatus.OK);
                    assertThat(resp.getBody()).isNotEmpty();
                })
                .expectComplete()
                .verify();
    }

}
