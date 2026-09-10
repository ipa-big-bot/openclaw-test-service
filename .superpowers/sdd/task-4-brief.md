# Task 4 Brief: Package and Run the Service with Docker Compose

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- Create: `.dockerignore`
- Create: `Dockerfile`
- Create: `compose.yaml`

## Interfaces
- Consumes: executable JAR output `target/openclaw-test-service-0.0.1-SNAPSHOT.jar` and health endpoint `/actuator/health`.
- Produces: Linux AMD64 image running as `app:app`; image health check; Compose service `app` on host port `8080`; image reference `ghcr.io/ipa-big/openclaw-test-service:${IMAGE_TAG:-latest}`.

## Steps

### Step 1: Confirm the container build is initially absent

Run:

```bash
docker build --platform linux/amd64 \
  -t openclaw-test-service:plan-test .
```

Expected: FAIL because the repository does not contain a `Dockerfile`.

### Step 2: Add the Docker build context exclusions

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

### Step 3: Add the two-stage non-root Docker image

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

### Step 4: Add Docker Compose local execution

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

### Step 5: Validate the Compose model

Run:

```bash
docker compose config
```

Expected: PASS and output containing image `ghcr.io/ipa-big/openclaw-test-service:latest`, port `8080`, restart policy `unless-stopped`, and the health check.

### Step 6: Build and inspect the image

Run:

```bash
docker build --platform linux/amd64 \
  -t openclaw-test-service:plan-test .
docker image inspect openclaw-test-service:plan-test \
  --format '{{.Config.User}}'
docker image inspect openclaw-test-service:plan-test \
  --format '{{json .Config.Healthcheck.Test}}'
```

Expected: the build succeeds; the first inspection prints `app:app`; the second contains `/actuator/health`.

### Step 7: Smoke-test the running image

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

Expected: health returns JSON containing `"status":"UP"`, OpenAPI returns JSON containing `"paths":{}`, Swagger UI contains `swagger`, and the named container stops cleanly.

### Step 8: Verify Docker Compose startup

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

Expected: Compose reports the `app` service as healthy, health returns status `UP`, and `docker compose down` removes the local service container and network.

### Step 9: Commit container packaging

```bash
git add .dockerignore Dockerfile compose.yaml
git commit -m "feat: add Docker and Compose packaging"
```

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 4 of the Spring Boot Service Scaffold plan. The goal is to package the service with Docker and Docker Compose.

## Global Constraints
- Run the container as a non-root user on port `8080`.
- Build Linux AMD64 images only.
- Publish `ghcr.io/ipa-big/openclaw-test-service` from `main` and Git tags matching `v*`.
