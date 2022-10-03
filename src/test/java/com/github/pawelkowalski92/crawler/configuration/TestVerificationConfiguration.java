package com.github.pawelkowalski92.crawler.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class TestVerificationConfiguration {

    @TestConfiguration
    public static class FailFastVerifierConfiguration {

        @Bean
        public FailFastClientVerifier failFastClientVerifier(@Qualifier("fail-fast") WebClient failFastWebClient) {
            return new FailFastClientVerifier(failFastWebClient);
        }

    }

    @TestConfiguration
    public static class SpyWebClientApplicatorConfiguration {

        @Bean
        public BeanPostProcessor spyWebClientBeanPostProcessor() {
            return new SpyWebClientBeanPostProcessor();
        }

    }

}
