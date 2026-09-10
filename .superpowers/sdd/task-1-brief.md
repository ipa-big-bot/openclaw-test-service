# Task 1 Brief: Create the Maven Application Foundation

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- `pom.xml`
- `.mvn/wrapper/maven-wrapper.properties`
- `.mvn/wrapper/maven-wrapper.jar`
- `mvnw`
- `mvnw.cmd`
- `.gitignore`
- `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplicationTests.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplication.java`

## Interfaces
- Consumes: Java 21 and a one-time Maven installation capable of running Maven Wrapper Plugin 3.3.4.
- Produces: `OpenclawTestServiceApplication`, an executable Spring Boot application entry point; `./mvnw`, pinned to Maven 3.9.16; Maven `test` and `verify` lifecycles used by all later tasks.

## Steps

### Step 1: Create the initial Maven build

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

### Step 2: Generate the pinned Maven Wrapper

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

### Step 3: Add repository ignore rules

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

### Step 4: Write the failing application startup test

Create `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplicationTests.java`:

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

### Step 5: Run the startup test to verify it fails

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=OpenclawTestServiceApplicationTests
```

Expected: FAIL because Spring Boot cannot find an `@SpringBootConfiguration` application class.

### Step 6: Add the minimal application entry point

Create `src/main/java/de/fraunhofer/ipa/openclawtestservice/OpenclawTestServiceApplication.java`:

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

### Step 7: Run the startup test to verify it passes

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=OpenclawTestServiceApplicationTests
```

Expected: PASS with one test and no failures or errors.

### Step 8: Commit the application foundation

```bash
git add pom.xml .mvn mvnw mvnw.cmd .gitignore src
git commit -m "feat: initialize Spring Boot service" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 1 of the Spring Boot Service Scaffold plan. The goal is to build a Java 21 Spring Boot web-service foundation with Actuator health endpoints, empty OpenAPI documentation and Swagger UI, Maven, Docker Compose, and a GitHub Actions pipeline that publishes images to GHCR.

## Global Constraints
- Use Java 21.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Use Spring Boot 4.1.1.
- Use Springdoc OpenAPI 3.1.1.
- Use Maven Wrapper 3.9.16.
- Do not add a business, sample, or weather API endpoint.
- Expose only Actuator `health` and `info` over HTTP.
- Serve OpenAPI JSON at `/v3/api-docs` and Swagger UI at `/swagger-ui/index.html`.
- Run the container as a non-root user on port `8080`.
- Build Linux AMD64 images only.
- Publish `ghcr.io/ipa-big/openclaw-test-service` from `main` and Git tags matching `v*`.
