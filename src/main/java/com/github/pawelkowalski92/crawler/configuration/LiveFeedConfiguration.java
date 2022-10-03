package com.github.pawelkowalski92.crawler.configuration;

import com.github.pawelkowalski92.crawler.model.CrawlingRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@ConfigurationProperties(prefix = "live-feed")
@ConstructorBinding
public record LiveFeedConfiguration(List<CrawlingRequest> sources) {
}