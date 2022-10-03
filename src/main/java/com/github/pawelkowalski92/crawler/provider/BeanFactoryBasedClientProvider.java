package com.github.pawelkowalski92.crawler.provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
public class BeanFactoryBasedClientProvider implements ClientProvider, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
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
        return beanFactory.getBean(WebClient.class);
    }

}
