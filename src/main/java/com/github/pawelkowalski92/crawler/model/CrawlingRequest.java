package com.github.pawelkowalski92.crawler.model;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

public record CrawlingRequest(URI resource, Optional<String> clientTag, Duration frequency) {
}
