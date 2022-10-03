package com.github.pawelkowalski92.crawler.configuration;

import com.github.pawelkowalski92.crawler.configuration.CrawlerClientsProperties.ClientDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class CrawlerClientsRegistrator implements BeanDefinitionRegistryPostProcessor {

    private final CrawlerClientsProperties clientsProperties;

    public CrawlerClientsRegistrator(CrawlerClientsProperties clientsProperties) {
        this.clientsProperties = clientsProperties;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        clientsProperties.definitions()
                .forEach(definition -> registry.registerBeanDefinition(
                        definition.tag(),
                        createCrawlerBeanDefinition(definition)
                ));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    private BeanDefinition createCrawlerBeanDefinition(ClientDefinition clientDefinition) {
        return BeanDefinitionBuilder.genericBeanDefinition()
                .setPrimary(clientDefinition.primary())
                .setFactoryMethodOnBean("createWebClient", "crawlerClientFactory")
                .addConstructorArgValue(clientDefinition)
                .getBeanDefinition();
    }

}
