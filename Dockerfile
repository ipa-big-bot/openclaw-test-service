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
