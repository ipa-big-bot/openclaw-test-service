package de.fraunhofer.ipa.openclawtestservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@Import(TestWebClientConfiguration.class)
class OpenclawTestServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

@TestConfiguration
class TestWebClientConfiguration {

    @Bean
    public WebClient.Builder testWebClientBuilder() {
        return WebClient.builder();
    }
}
