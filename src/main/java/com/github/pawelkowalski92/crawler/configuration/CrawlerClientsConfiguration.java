package com.github.pawelkowalski92.crawler.configuration;

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CrawlerClientsConfiguration {

    public static final String WEB_CRAWLERS_ROOT = "web-crawlers";

    @Bean
    public static CrawlerClientsProperties crawlerClientsProperties(Environment environment) {
        return Binder.get(environment)
                .bind(WEB_CRAWLERS_ROOT, CrawlerClientsProperties.class)
                .get();
    }

    @Bean
    public static BeanDefinitionRegistryPostProcessor crawlerClientsRegistrator(CrawlerClientsProperties configuration) {
        return new CrawlerClientsRegistrator(configuration);
    }

}
