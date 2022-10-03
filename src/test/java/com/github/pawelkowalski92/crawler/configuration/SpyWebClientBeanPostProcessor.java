package com.github.pawelkowalski92.crawler.configuration;

import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class SpyWebClientBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean instanceof WebClient
                ? Mockito.spy(bean)
                : bean;
    }

}
