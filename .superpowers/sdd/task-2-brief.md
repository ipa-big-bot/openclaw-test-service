# Task 2 Brief: Expose and Verify Actuator Health

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create/Modify
- Modify: `pom.xml` (add Actuator dependency)
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java`

## Interfaces
- Consumes: `OpenclawTestServiceApplication` and the Failsafe `*IT` lifecycle from Task 1.
- Produces: HTTP `GET /actuator/health` returning `200` and status `UP`; HTTP `GET /actuator/info`; explicit exposure of only `health` and `info`.

## Steps

### Step 1: Write the failing HTTP health integration test

Create `src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java`:

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
class HealthEndpointIT {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void healthEndpointReportsUp() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/actuator/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }
}
```

### Step 2: Run the health integration test to verify it fails

Run:

```bash
./mvnw --batch-mode verify -Dit.test=HealthEndpointIT
```

Expected: FAIL because `/actuator/health` returns `404` before Actuator is added.

### Step 3: Add Actuator to the Maven dependencies

Insert this dependency after `spring-boot-starter-webmvc` in `pom.xml`:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

### Step 4: Configure the application and endpoint exposure

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: openclaw-test-service

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
  info:
    env:
      enabled: true

info:
  app:
    name: ${spring.application.name}
    description: Spring Boot web-service foundation
```

### Step 5: Run the health and startup tests

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS; the context test and `HealthEndpointIT` both succeed.

### Step 6: Commit the operational endpoint

```bash
git add pom.xml src/main/resources/application.yml \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java
git commit -m "feat: add actuator health endpoint"
```

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 2 of the Spring Boot Service Scaffold plan. The goal is to add Actuator health endpoints with proper configuration.

## Global Constraints
- Use Java 21.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Use Spring Boot 4.1.1.
- Expose only Actuator `health` and `info` over HTTP.
