# Task 3 Brief: Add and Verify OpenAPI Documentation

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create/Modify
- Modify: `pom.xml` (add Springdoc dependency)
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java`
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`

## Interfaces
- Consumes: the running Spring MVC application from Task 1.
- Produces: `GET /v3/api-docs`, `GET /swagger-ui/index.html`, OpenAPI title `OpenClaw Test Service API`, version `v1`, and an empty `paths` object.

## Steps

### Step 1: Write the failing OpenAPI and Swagger UI integration test

Create `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`:

```java
package de.fraunhofer.ipa.openclawtestservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiEndpointIT {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void openApiDocumentContainsMetadataAndNoBusinessPaths()
            throws IOException, InterruptedException {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"openapi\":")
                .contains("\"title\":\"OpenClaw Test Service API\"")
                .contains("\"version\":\"v1\"")
                .contains("\"paths\":{}");
    }

    @Test
    void swaggerUiIsAccessible() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/swagger-ui/index.html");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).containsIgnoringCase("swagger");
    }

    private HttpResponse<String> get(String path)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
```

### Step 2: Run the OpenAPI integration test to verify it fails

Run:

```bash
./mvnw --batch-mode verify -Dit.test=OpenApiEndpointIT
```

Expected: FAIL because `/v3/api-docs` and `/swagger-ui/index.html` return `404`.

### Step 3: Add the Springdoc dependency

Insert this dependency after `spring-boot-starter-actuator` in `pom.xml`:

```xml
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
```

### Step 4: Define service-level OpenAPI metadata

Create `src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java`:

```java
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
```

### Step 5: Run all Maven verification

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS; startup, health, OpenAPI JSON, and Swagger UI tests all succeed.

### Step 6: Commit API documentation support

```bash
git add pom.xml \
  src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java
git commit -m "feat: add OpenAPI documentation"
```

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 3 of the Spring Boot Service Scaffold plan. The goal is to add OpenAPI documentation and Swagger UI.

## Global Constraints
- Use Java 21.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Use Spring Boot 4.1.1.
- Use Springdoc OpenAPI 3.1.1.
- Expose OpenAPI JSON at `/v3/api-docs`.
- Expose Swagger UI at `/swagger-ui/index.html`.
