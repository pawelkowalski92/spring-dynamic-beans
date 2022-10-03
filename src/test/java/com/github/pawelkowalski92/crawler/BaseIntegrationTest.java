package com.github.pawelkowalski92.crawler;

import com.github.pawelkowalski92.crawler.configuration.TestClockConfiguration;
import com.github.pawelkowalski92.crawler.configuration.TestVerificationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
@DirtiesContext
@Import({
        TestClockConfiguration.class,
        TestVerificationConfiguration.SpyWebClientApplicatorConfiguration.class
})
public class BaseIntegrationTest {

    @Container
    static final GenericContainer<?> API_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("kennethreitz/httpbin")
    ).withExposedPorts(80);

    static String getContainerBaseURI() {
        return API_CONTAINER.getHost() + ":" + API_CONTAINER.getMappedPort(80);
    }

    @DynamicPropertySource
    static void apiConfigurer(DynamicPropertyRegistry registry) {
        registry.add("base-uri", BaseIntegrationTest::getContainerBaseURI);
    }

    @Value("${base-uri}")
    protected String baseUri;

    @Autowired
    private BeanFactory beanFactory;

    @AfterEach
    public void resetInvocations() {
        WebClient[] clients = beanFactory.getBeanProvider(WebClient.class)
                .stream()
                .toArray(WebClient[]::new);
        Mockito.clearInvocations(clients);
    }

    protected WebClient resolveClient(String clientTag) {
        return beanFactory.getBean(clientTag, WebClient.class);
    }

    protected WebClient resolveDefaultClient() {
        return beanFactory.getBean(WebClient.class);
    }

    protected <T> Predicate<T> eventsReceivedBelow(int limit) {
        AtomicInteger counter = new AtomicInteger(0);
        return __ -> counter.getAndIncrement() < limit;
    }

}
