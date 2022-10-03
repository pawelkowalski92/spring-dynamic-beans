package com.github.pawelkowalski92.crawler.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfiguration {

    public static final Instant FIXED_TIME = Instant.now();

    @Bean
    public Clock clock() {
        return Clock.fixed(FIXED_TIME, ZoneId.systemDefault());
    }

}
