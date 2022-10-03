package com.github.pawelkowalski92.crawler.provider;

import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties;
import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties.ClientDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CrawlerClientsWrapper implements ClientProvider, BeanFactoryAware {
    private final CrawlerClientFactory clientFactory;
    private final CrawlerClientsProperties clientsProperties;

    private ConfigurableListableBeanFactory beanFactory;

    public CrawlerClientsWrapper(CrawlerClientFactory clientFactory, CrawlerClientsProperties clientsProperties) {
        this.clientFactory = clientFactory;
        this.clientsProperties = clientsProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isTrue(beanFactory instanceof ConfigurableListableBeanFactory, () -> "Bean factory is not configurable!");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Map<String, WebClient> configuredClients = clientsProperties.definitions()
                .stream()
                .collect(Collectors.toMap(
                        ClientDefinition::tag,
                        clientFactory::createWebClient
                ));
        configuredClients.forEach(beanFactory::registerSingleton);
    }

    @Override
    public Optional<WebClient> findClient(String tag) {
        try {
            return Optional.of(beanFactory.getBean(tag, WebClient.class));
        } catch (NoSuchBeanDefinitionException ex) {
            return Optional.empty();
        }
    }

    @Override
    public WebClient getDefaultClient() {
        return findPrimaryDefinition()
                .map(ClientDefinition::tag)
                .flatMap(this::findClient)
                .orElseGet(() -> beanFactory.getBean(WebClient.class));
    }

    private Optional<ClientDefinition> findPrimaryDefinition() {
        return clientsProperties.definitions()
                .stream()
                .filter(ClientDefinition::primary)
                .findFirst();
    }

}
