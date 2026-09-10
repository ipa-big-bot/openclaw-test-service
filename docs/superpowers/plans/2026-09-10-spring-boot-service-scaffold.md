# Spring Boot Service Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Java 21 Spring Boot web-service foundation with Actuator health endpoints, empty OpenAPI documentation and Swagger UI, Maven, Docker Compose, and a GitHub Actions pipeline that publishes images to GHCR.

**Architecture:** Use one executable Spring Boot Maven module with no business controller. Add operational and documentation behavior through Spring Boot Actuator and Springdoc, package the JAR in a two-stage non-root container, and keep verification before publication in GitHub Actions.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Maven 3.9.16 Wrapper, Springdoc OpenAPI 3.1.1, JUnit 5, Docker, Docker Compose, GitHub Actions, GHCR

## Global Constraints

- Use Java 21.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Use Spring Boot 4.1.1, the newest GA Spring Boot release compatible with Java 21 when this plan was written.
- Use Springdoc OpenAPI 3.1.1, the newest GA release compatible with the selected Spring Boot release when this plan was written.
- Use Maven Wrapper 3.9.16; do not require system Maven after wrapper generation.
- Do not add a business, sample, or weather API endpoint.
- Expose only Actuator `health` and `info` over HTTP.
- Serve OpenAPI JSON at `/v3/api-docs` and Swagger UI at `/swagger-ui/index.html`.
- Run the container as a non-root user on port `8080`.
- Build Linux AMD64 images only.
- Publish `ghcr.io/ipa-big/openclaw-test-service` from `main` and Git tags matching `v*`.
- Do not add database, messaging, authentication, Kubernetes, or multi-module configuration.

---

## File Structure

| File | Responsibility |
|---|---|
| `pom.xml` | Defines the Java 21 Spring Boot build, runtime dependencies, test dependencies, executable JAR packaging, and Failsafe integration-test lifecycle. |
| `.mvn/wrapper/maven-wrapper.properties` | Pins Maven 3.9.16 for reproducible wrapper builds. |
| `.mvn/wrapper/maven-wrapper.jar` | Runs the Maven Wrapper without requiring a system Maven installation. |
| `mvnw` | Unix Maven Wrapper launcher. |
| `mvnw.cmd` | Windows Maven Wrapper launcher. |
| `.gitignore` | Excludes Maven output, IDE files, logs, and local environment files from version history. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplication.java` | Starts the Spring Boot application. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java` | Defines service-level OpenAPI metadata without adding operations. |
| `src/main/resources/application.yml` | Defines the application name and externally exposed Actuator endpoints. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplicationTests.java` | Proves that the Spring application context starts. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java` | Proves that a running application serves an `UP` health response over HTTP. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java` | Proves that OpenAPI JSON and Swagger UI are reachable and no business paths exist. |
| `.dockerignore` | Keeps build output and repository-only files out of the Docker build context. |
| `Dockerfile` | Builds the executable JAR and creates a non-root Java 21 runtime image with a health check. |
| `compose.yaml` | Builds or runs the service locally with port mapping, restart behavior, and health monitoring. |
| `.github/workflows/ci.yml` | Verifies Maven and Docker builds on pull requests and publishes GHCR images on `main` and `v*` tags. |
| `README.md` | Documents local Maven, JAR, Docker, Compose, endpoint, and GHCR usage. |

---

### Task 1: Create the Maven Application Foundation

**Files:**
- Create: `pom.xml`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `.mvn/wrapper/maven-wrapper.jar`
- Create: `mvnw`
- Create: `mvnw.cmd`
- Create: `.gitignore`
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplicationTests.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplication.java`

**Interfaces:**
- Consumes: Java 21 and a one-time Maven installation capable of running Maven Wrapper Plugin 3.3.4.
- Produces: `OpenclawTestServiceApplication`, an executable Spring Boot application entry point; `./mvnw`, pinned to Maven 3.9.16; Maven `test` and `verify` lifecycles used by all later tasks.

- [ ] **Step 1: Create the initial Maven build**

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>

    <groupId>de.fraunhofer.ipa</groupId>
    <artifactId>openclaw-test-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>openclaw-test-service</name>
    <description>Spring Boot web-service foundation</description>

    <properties>
        <java.version>21</java.version>
        <springdoc.version>3.1.1</springdoc.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Generate the pinned Maven Wrapper**

Run:

```bash
mvn org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper \
  -Dmaven=3.9.16 \
  -Dtype=bin
chmod +x mvnw
```

Expected: Maven creates `.mvn/wrapper/maven-wrapper.properties`,
`.mvn/wrapper/maven-wrapper.jar`, `mvnw`, and `mvnw.cmd`; the properties file
references Maven `3.9.16`.

- [ ] **Step 3: Add repository ignore rules**

Create `.gitignore`:

```gitignore
target/

.idea/
.vscode/
*.iml
.classpath
.project
.settings/

*.log
logs/

.env
.env.*
!.env.example

.DS_Store
Thumbs.db
```

Run:

```bash
git check-ignore target/example.jar .idea/workspace.xml local.log .env
test -f .mvn/wrapper/maven-wrapper.jar
```

Expected: `git check-ignore` prints all four ignored paths, and the wrapper JAR
exists.

- [ ] **Step 4: Write the failing application startup test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplicationTests.java`:

```java
package de.fraunhofer.ipa.openclawtestservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenclawTestServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 5: Run the startup test to verify it fails**

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=OpenclawTestServiceApplicationTests
```

Expected: FAIL because Spring Boot cannot find an
`@SpringBootConfiguration` application class.

- [ ] **Step 6: Add the minimal application entry point**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplication.java`:

```java
package de.fraunhofer.ipa.openclawtestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenclawTestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenclawTestServiceApplication.class, args);
    }
}
```

- [ ] **Step 7: Run the startup test to verify it passes**

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=OpenclawTestServiceApplicationTests
```

Expected: PASS with one test and no failures or errors.

- [ ] **Step 8: Commit the application foundation**

```bash
git add pom.xml .mvn mvnw mvnw.cmd .gitignore src
git commit -m "feat: initialize Spring Boot service" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Expose and Verify Actuator Health

**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java`

**Interfaces:**
- Consumes: `OpenclawTestServiceApplication` and the Failsafe `*IT` lifecycle from Task 1.
- Produces: HTTP `GET /actuator/health` returning `200` and status `UP`; HTTP `GET /actuator/info`; explicit exposure of only `health` and `info`.

- [ ] **Step 1: Write the failing HTTP health integration test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java`:

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

- [ ] **Step 2: Run the health integration test to verify it fails**

Run:

```bash
./mvnw --batch-mode verify -Dit.test=HealthEndpointIT
```

Expected: FAIL because `/actuator/health` returns `404` before Actuator is
added.

- [ ] **Step 3: Add Actuator to the Maven dependencies**

Insert this dependency after `spring-boot-starter-webmvc` in `pom.xml`:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

- [ ] **Step 4: Configure the application and endpoint exposure**

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

- [ ] **Step 5: Run the health and startup tests**

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS; the context test and `HealthEndpointIT` both succeed.

- [ ] **Step 6: Commit the operational endpoint**

```bash
git add pom.xml src/main/resources/application.yml \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/HealthEndpointIT.java
git commit -m "feat: add actuator health endpoint" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 3: Add and Verify OpenAPI Documentation

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java`
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`

**Interfaces:**
- Consumes: the running Spring MVC application from Task 1.
- Produces: `GET /v3/api-docs`, `GET /swagger-ui/index.html`, OpenAPI title `OpenClaw Test Service API`, version `v1`, and an empty `paths` object.

- [ ] **Step 1: Write the failing OpenAPI and Swagger UI integration test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`:

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

- [ ] **Step 2: Run the OpenAPI integration test to verify it fails**

Run:

```bash
./mvnw --batch-mode verify -Dit.test=OpenApiEndpointIT
```

Expected: FAIL because `/v3/api-docs` and `/swagger-ui/index.html` return
`404`.

- [ ] **Step 3: Add the Springdoc dependency**

Insert this dependency after `spring-boot-starter-actuator` in `pom.xml`:

```xml
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
```

- [ ] **Step 4: Define service-level OpenAPI metadata**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java`:

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

- [ ] **Step 5: Run all Maven verification**

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS; startup, health, OpenAPI JSON, and Swagger UI tests all
succeed.

- [ ] **Step 6: Commit API documentation support**

```bash
git add pom.xml \
  src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java
git commit -m "feat: add OpenAPI documentation" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Package and Run the Service with Docker Compose

**Files:**
- Create: `.dockerignore`
- Create: `Dockerfile`
- Create: `compose.yaml`

**Interfaces:**
- Consumes: executable JAR output
  `target/openclaw-test-service-0.0.1-SNAPSHOT.jar` and health endpoint
  `/actuator/health`.
- Produces: Linux AMD64 image running as `app:app`; image health check; Compose service `app` on host port `8080`; image reference `ghcr.io/ipa-big/openclaw-test-service:${IMAGE_TAG:-latest}`.

- [ ] **Step 1: Confirm the container build is initially absent**

Run:

```bash
docker build --platform linux/amd64 \
  -t openclaw-test-service:plan-test .
```

Expected: FAIL because the repository does not contain a `Dockerfile`.

- [ ] **Step 2: Add the Docker build context exclusions**

Create `.dockerignore`:

```dockerignore
target/
.git/
.github/
.idea/
.vscode/
*.iml
*.log
.env
.env.*
docs/
README.md
compose.yaml
```

- [ ] **Step 3: Add the two-stage non-root Docker image**

Create `Dockerfile`:

```dockerfile
FROM maven:3.9.16-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw --batch-mode dependency:go-offline

COPY src/ src/
RUN ./mvnw --batch-mode package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

COPY --from=builder \
    /workspace/target/openclaw-test-service-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER app:app

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=3 \
    CMD wget -q -O - http://localhost:8080/actuator/health \
        | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **Step 4: Add Docker Compose local execution**

Create `compose.yaml`:

```yaml
services:
  app:
    image: ghcr.io/ipa-big/openclaw-test-service:${IMAGE_TAG:-latest}
    build:
      context: .
    ports:
      - "8080:8080"
    restart: unless-stopped
    healthcheck:
      test:
        - CMD-SHELL
        - >-
          wget -q -O - http://localhost:8080/actuator/health
          | grep -q '"status":"UP"'
      interval: 10s
      timeout: 3s
      start_period: 20s
      retries: 3
```

- [ ] **Step 5: Validate the Compose model**

Run:

```bash
docker compose config
```

Expected: PASS and output containing image
`ghcr.io/ipa-big/openclaw-test-service:latest`, port `8080`, restart policy
`unless-stopped`, and the health check.

- [ ] **Step 6: Build and inspect the image**

Run:

```bash
docker build --platform linux/amd64 \
  -t openclaw-test-service:plan-test .
docker image inspect openclaw-test-service:plan-test \
  --format '{{.Config.User}}'
docker image inspect openclaw-test-service:plan-test \
  --format '{{json .Config.Healthcheck.Test}}'
```

Expected: the build succeeds; the first inspection prints `app:app`; the
second contains `/actuator/health`.

- [ ] **Step 7: Smoke-test the running image**

Run:

```bash
docker run --detach --rm \
  --name openclaw-test-service-plan-test \
  --publish 18080:8080 \
  openclaw-test-service:plan-test

for attempt in $(seq 1 30); do
  if curl --fail --silent http://localhost:18080/actuator/health \
      | grep -q '"status":"UP"'; then
    break
  fi
  sleep 1
done

curl --fail --silent http://localhost:18080/actuator/health
curl --fail --silent http://localhost:18080/v3/api-docs
curl --fail --silent http://localhost:18080/swagger-ui/index.html \
  | grep -qi swagger

docker stop openclaw-test-service-plan-test
```

Expected: health returns JSON containing `"status":"UP"`, OpenAPI returns JSON
containing `"paths":{}`, Swagger UI contains `swagger`, and the named container
stops cleanly.

- [ ] **Step 8: Verify Docker Compose startup**

Run:

```bash
docker compose up --build --detach

for attempt in $(seq 1 30); do
  if docker compose ps --format json | grep -q '"Health":"healthy"'; then
    break
  fi
  sleep 1
done

curl --fail --silent http://localhost:8080/actuator/health
docker compose down
```

Expected: Compose reports the `app` service as healthy, health returns status
`UP`, and `docker compose down` removes the local service container and network.

- [ ] **Step 9: Commit container packaging**

```bash
git add .dockerignore Dockerfile compose.yaml
git commit -m "feat: add Docker and Compose packaging" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 5: Add GitHub Actions Verification and GHCR Publication

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: `./mvnw --batch-mode verify`, `Dockerfile`, branch `main`, tags matching `v*`, and GitHub-provided `GITHUB_TOKEN`.
- Produces: pull-request Maven and image verification; GHCR tags `latest`, the original `v*` tag, and `sha-<short-commit-sha>`; image repository `ghcr.io/ipa-big/openclaw-test-service`.

- [ ] **Step 1: Define a failing workflow-presence check**

Run:

```bash
test -f .github/workflows/ci.yml \
  && grep -q 'packages: write' .github/workflows/ci.yml \
  && grep -q 'docker/build-push-action@v6' .github/workflows/ci.yml
```

Expected: FAIL because `.github/workflows/ci.yml` does not exist.

- [ ] **Step 2: Add the CI and publication workflow**

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  pull_request:
  push:
    branches:
      - main
    tags:
      - "v*"

permissions:
  contents: read

env:
  IMAGE_NAME: ghcr.io/${{ github.repository }}

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - name: Check out repository
        uses: actions/checkout@v5

      - name: Set up Java
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: "21"
          cache: maven

      - name: Verify Maven project
        run: ./mvnw --batch-mode verify

  image-build:
    if: github.event_name == 'pull_request'
    needs: verify
    runs-on: ubuntu-latest
    steps:
      - name: Check out repository
        uses: actions/checkout@v5

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Verify container image build
        uses: docker/build-push-action@v6
        with:
          context: .
          platforms: linux/amd64
          push: false
          tags: openclaw-test-service:pr-${{ github.event.pull_request.number }}

  image-publish:
    if: github.event_name == 'push'
    needs: verify
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - name: Check out repository
        uses: actions/checkout@v5

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ github.token }}

      - name: Generate image metadata
        id: metadata
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.IMAGE_NAME }}
          tags: |
            type=raw,value=latest,enable=${{ github.ref == 'refs/heads/main' }}
            type=ref,event=tag
            type=sha,format=short

      - name: Build and publish container image
        uses: docker/build-push-action@v6
        with:
          context: .
          platforms: linux/amd64
          push: true
          tags: ${{ steps.metadata.outputs.tags }}
          labels: ${{ steps.metadata.outputs.labels }}
```

- [ ] **Step 3: Verify workflow triggers, permissions, and actions**

Run:

```bash
test -f .github/workflows/ci.yml
grep -q 'java-version: "21"' .github/workflows/ci.yml
grep -q 'packages: write' .github/workflows/ci.yml
grep -q 'platforms: linux/amd64' .github/workflows/ci.yml
grep -q 'type=raw,value=latest' .github/workflows/ci.yml
grep -q 'type=ref,event=tag' .github/workflows/ci.yml
grep -q 'type=sha,format=short' .github/workflows/ci.yml
grep -q './mvnw --batch-mode verify' .github/workflows/ci.yml
git diff --check
```

Expected: every command succeeds with no output from `git diff --check`.

- [ ] **Step 4: Commit the GitHub Actions workflow**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: verify and publish service image" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 6: Document Usage and Run Final Verification

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: Maven, endpoint, Docker, Compose, workflow, and GHCR behavior from Tasks 1-5.
- Produces: operator and developer instructions matching the implemented commands and URLs.

- [ ] **Step 1: Replace the existing README with complete usage documentation**

Replace `README.md` with:

````markdown
# OpenClaw Test Service

Spring Boot foundation for a web service with operational health endpoints,
OpenAPI documentation, Docker packaging, and GHCR publication.

The initial application intentionally contains no business API.

## Requirements

- Java 21
- Docker with Docker Compose for container-based execution

Maven is provided through the Maven Wrapper.

## Build and Test

```bash
./mvnw --batch-mode verify
```

The verification suite checks that the Spring application starts and that the
running health endpoint reports `UP`.

## Run with Maven

```bash
./mvnw spring-boot:run
```

## Run the Packaged JAR

```bash
./mvnw --batch-mode package
java -jar target/openclaw-test-service-0.0.1-SNAPSHOT.jar
```

## Service Endpoints

| Purpose | URL |
|---|---|
| Health | <http://localhost:8080/actuator/health> |
| Info | <http://localhost:8080/actuator/info> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |

The OpenAPI document has no business operations until controllers are added.

## Docker

Build the Linux AMD64 image:

```bash
docker build --platform linux/amd64 \
  -t openclaw-test-service:local .
```

Run it:

```bash
docker run --rm --publish 8080:8080 \
  openclaw-test-service:local
```

The runtime image uses a non-root user and checks
`/actuator/health` for container health.

## Docker Compose

Build and run the local source:

```bash
docker compose up --build
```

Stop the service:

```bash
docker compose down
```

Run a published tag:

```bash
IMAGE_TAG=v1.0.0 docker compose pull
IMAGE_TAG=v1.0.0 docker compose up --no-build
```

## GitHub Container Registry

The GitHub Actions workflow publishes:

- `ghcr.io/ipa-big/openclaw-test-service:latest` from `main`
- `ghcr.io/ipa-big/openclaw-test-service:<git-tag>` from tags matching `v*`
- `ghcr.io/ipa-big/openclaw-test-service:sha-<short-commit-sha>` from published
  commits

Pull requests run Maven verification and build the image without publishing it.
````

- [ ] **Step 2: Run the complete Maven verification**

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS with the context test, health integration test, and both OpenAPI
integration test methods succeeding.

- [ ] **Step 3: Run the complete container verification**

Run:

```bash
docker compose up --build --detach

for attempt in $(seq 1 30); do
  if curl --fail --silent http://localhost:8080/actuator/health \
      | grep -q '"status":"UP"'; then
    break
  fi
  sleep 1
done

curl --fail --silent http://localhost:8080/actuator/health \
  | grep -q '"status":"UP"'
curl --fail --silent http://localhost:8080/actuator/info
curl --fail --silent http://localhost:8080/v3/api-docs \
  | grep -q '"paths":{}'
curl --fail --silent http://localhost:8080/swagger-ui/index.html \
  | grep -qi swagger
docker compose ps --format json | grep -q '"Health":"healthy"'

docker compose down
```

Expected: every endpoint check succeeds, Compose reports the service as
healthy, and the environment shuts down cleanly.

- [ ] **Step 4: Verify repository hygiene and workflow content**

Run:

```bash
git check-ignore target/example.jar .idea/workspace.xml local.log .env
test -f .mvn/wrapper/maven-wrapper.jar
test -x mvnw
grep -q 'packages: write' .github/workflows/ci.yml
grep -q 'platforms: linux/amd64' .github/workflows/ci.yml
git diff --check
git status --short
```

Expected: ignored paths are printed, required wrapper and workflow checks
succeed, `git diff --check` prints nothing, and `git status --short` lists only
the intended README change before the commit.

- [ ] **Step 5: Commit the documentation**

```bash
git add README.md
git commit -m "docs: document service development and delivery" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

- [ ] **Step 6: Confirm the final commit set**

Run:

```bash
git status --short
git --no-pager log --oneline -6
```

Expected: the worktree is clean and the six implementation commits are visible
in reverse order:

```text
docs: document service development and delivery
ci: verify and publish service image
feat: add Docker and Compose packaging
feat: add OpenAPI documentation
feat: add actuator health endpoint
feat: initialize Spring Boot service
```
