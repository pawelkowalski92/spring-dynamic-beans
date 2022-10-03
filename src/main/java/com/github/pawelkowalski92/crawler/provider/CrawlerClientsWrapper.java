package com.github.pawelkowalski92.crawler.provider;

import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties;
import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties.ClientDefinition;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CrawlerClientsWrapper implements ClientProvider {
    private final CrawlerClientFactory clientFactory;
    private final CrawlerClientsProperties clientsProperties;

    private final Map<String, WebClient> configuredClients = new HashMap<>();
    private WebClient defaultClient;

    public CrawlerClientsWrapper(CrawlerClientFactory clientFactory, CrawlerClientsProperties clientsProperties) {
        this.clientFactory = clientFactory;
        this.clientsProperties = clientsProperties;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Map<String, WebClient> configuredClients = clientsProperties.definitions()
                .stream()
                .collect(Collectors.toMap(
                        ClientDefinition::tag,
                        clientFactory::createWebClient
                ));
        this.configuredClients.putAll(configuredClients);
        this.defaultClient = findFirstPrimaryFromConfiguration().or(this::findFirstFromConfiguration)
                .orElse(null);
    }

    @Override
    public Optional<WebClient> findClient(String tag) {
        return Optional.ofNullable(configuredClients.get(tag));
    }

    @Override
    public WebClient getDefaultClient() {
        return defaultClient;
    }

    private Optional<WebClient> findFirstPrimaryFromConfiguration() {
        return clientsProperties.definitions()
                .stream()
                .filter(ClientDefinition::primary)
                .map(clientFactory::createWebClient)
                .findFirst();
    }

    private Optional<WebClient> findFirstFromConfiguration() {
        return configuredClients.values()
                .stream()
                .findFirst();
    }

}
