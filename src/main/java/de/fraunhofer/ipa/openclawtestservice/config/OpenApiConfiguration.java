package de.fraunhofer.ipa.openclawtestservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI serviceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OpenClaw Test Service API")
                        .version("v1")
                        .description("API documentation for openclaw-test-service."));
    }
}
