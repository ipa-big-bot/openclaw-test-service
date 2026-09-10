# Spring Boot Service Scaffold Design

## Purpose

Create a production-oriented foundation for `openclaw-test-service` without
inventing a business API. The repository will contain a runnable Spring Boot
web service, operational health endpoints, generated OpenAPI documentation,
container packaging, local Docker Compose support, and a GitHub Actions
pipeline that publishes images to GitHub Container Registry (GHCR).

## Scope

The initial service provides infrastructure and operational capabilities only.
It does not define a sample controller or domain behavior.

Included:

- Java 21 and Maven with the Maven Wrapper
- A single Spring Boot application module
- Spring Boot Actuator health and info endpoints
- Generated OpenAPI JSON and Swagger UI
- Automated startup and health endpoint tests
- A multi-stage Docker image running as a non-root user
- Docker Compose configuration for local execution
- GitHub Actions verification and GHCR publication
- Repository ignore rules and usage documentation

Excluded:

- Business or weather API endpoints
- Database, messaging, authentication, or authorization
- Kubernetes-specific probes or deployment manifests
- Multi-module Maven structure
- Multi-architecture container builds

## Technology Baseline

- Java: 21
- Build tool: Maven
- Maven coordinates:
  - `groupId`: `de.fraunhofer.ipa`
  - `artifactId`: `openclaw-test-service`
- Java base package: `de.fraunhofer.ipa.openclawtestservice`
- Spring Boot: the newest generally available stable release at implementation
  time whose documented runtime compatibility includes Java 21
- OpenAPI integration: the newest generally available Springdoc release
  documented as compatible with the selected Spring Boot release

Milestone, release-candidate, snapshot, and other pre-release dependencies will
not be used.

## Architecture and Components

### Application

The application is a single executable Spring Boot JAR. It includes:

- Spring Web for the servlet-based web runtime
- Spring Boot Actuator for operational endpoints
- Springdoc OpenAPI with Swagger UI
- Spring Boot Test for automated verification

The application has one main class in the base package. No controller, service,
repository, or domain package is created until a real business capability
requires one.

### Operational Endpoints

The service exposes only Actuator `health` and `info` over HTTP. The expected
health endpoint is:

```text
GET /actuator/health
```

A healthy service responds with HTTP `200` and Actuator status `UP`. Other
Actuator endpoints remain unavailable over HTTP by default. Kubernetes-specific
liveness and readiness probe groups are not enabled.

### API Documentation

Springdoc generates the OpenAPI document from the application context:

```text
GET /v3/api-docs
GET /swagger-ui/index.html
```

Because the initial service has no business controllers, the OpenAPI document
contains service metadata and no business operations. Actuator endpoints are
not presented as the business API. Future Spring MVC controllers will be
discovered automatically without requiring a handwritten OpenAPI file.

### Repository Assets

The repository will contain:

- `pom.xml`
- Maven Wrapper scripts and wrapper configuration
- Application source and test source
- `application.yml`
- `.gitignore`
- `.dockerignore`
- `Dockerfile`
- `compose.yaml`
- `.github/workflows/ci.yml`
- An updated `README.md`

The `.gitignore` excludes Maven output, IDE metadata, log files, and local
environment files that must not enter version history. It does not exclude the
Maven Wrapper JAR or other files required for reproducible builds.

## Runtime and Container Design

The `Dockerfile` uses two stages:

1. A Java 21 Maven builder runs the Maven package lifecycle and creates the
   executable JAR.
2. An Alpine-based Java 21 JRE image receives only the packaged JAR and runtime
   configuration.

The runtime stage:

- Runs under a dedicated non-root user
- Exposes port `8080`
- Starts the JAR directly
- Defines a health check against `/actuator/health`
- Contains no source tree or Maven installation

Alpine's built-in HTTP tooling is used by the image health check, avoiding an
additional package installation. CI runs the tests before the image build; the
builder stage packages with tests skipped to avoid running the same suite twice.

`compose.yaml` defines one application service with:

- Image name `ghcr.io/ipa-big/openclaw-test-service`
- Local build context pointing at the repository root
- Image reference
  `ghcr.io/ipa-big/openclaw-test-service:${IMAGE_TAG:-latest}`
- Host-to-container port mapping `8080:8080`
- The same Actuator-based health check as the image
- Restart policy `unless-stopped`

Running `docker compose up --build` builds and starts the local source. The
published image can be selected with `IMAGE_TAG=<tag> docker compose pull`
followed by `IMAGE_TAG=<tag> docker compose up --no-build`. No external
services or Docker volumes are required.

## Request and Failure Flow

An HTTP health request is handled directly by Spring Boot Actuator and returns
Actuator's standard JSON response. Swagger UI requests the generated OpenAPI
document from `/v3/api-docs`; no handwritten document is synchronized at
runtime.

Application startup and configuration errors terminate the process with a
non-zero status. No startup script or application code converts failures into
successful responses. Docker and Compose distinguish a running process from a
healthy service through the health check.

The GitHub Actions pipeline is fail-fast by dependency:

1. Maven verification must succeed.
2. The container image must build successfully.
3. Publication occurs only after both previous steps succeed.

No global exception handler is introduced because there is no business API or
application-level error contract yet.

## Automated Testing

The Maven `verify` lifecycle runs two explicit checks:

1. A Spring context test verifies that the application starts successfully.
2. A random-port HTTP integration test requests `/actuator/health` and verifies
   HTTP `200` and Actuator status `UP`.

The health endpoint test exercises the running HTTP stack rather than invoking
an Actuator component directly. Local development and GitHub Actions use the
same command:

```text
./mvnw --batch-mode verify
```

## GitHub Actions and GHCR

A single workflow runs for:

- Pull requests
- Pushes to `main`
- Version tags matching `v*`

Every run:

1. Checks out the repository.
2. Sets up Java 21 with Maven dependency caching.
3. Runs `./mvnw --batch-mode verify`.
4. Configures Docker Buildx.
5. Builds the Docker image.

Pull requests build but do not publish the image. Pushes to `main` and version
tags authenticate to GHCR with the workflow `GITHUB_TOKEN` and publish:

- `main`: `latest` and `sha-<short-commit-sha>`
- Version tag: the original Git tag and `sha-<short-commit-sha>`

The image repository is:

```text
ghcr.io/ipa-big/openclaw-test-service
```

Workflow permissions are limited to `contents: read` and `packages: write`.
The build targets the default Linux AMD64 platform only.

## Documentation

The README will document:

- Prerequisites
- Maven verification and local startup
- Direct JAR execution
- Health endpoint URL
- OpenAPI JSON and Swagger UI URLs
- Docker image build and run commands
- Docker Compose startup and shutdown
- GHCR image naming and tag behavior

## Acceptance Criteria

The design is satisfied when:

- `./mvnw --batch-mode verify` succeeds on Java 21.
- The automated tests prove application startup and HTTP accessibility of the
  health endpoint with status `UP`.
- The running application serves `/actuator/health`, `/actuator/info`,
  `/v3/api-docs`, and `/swagger-ui/index.html`.
- The generated OpenAPI document is valid and contains no fabricated business
  operations.
- The Docker image builds, runs as a non-root user, and becomes healthy.
- `docker compose up --build` starts a healthy service on host port `8080`.
- Pull requests verify Maven and Docker builds without publishing.
- Pushes to `main` and Git tags matching `v*` publish the specified tags to
  GHCR.
- Maven build output and common local artifacts are excluded from version
  history.
