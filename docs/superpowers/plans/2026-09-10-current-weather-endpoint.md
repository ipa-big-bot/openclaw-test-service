# Current Weather Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `GET /api/v1/weather/current`, resolving a city or coordinates through Open-Meteo and returning a stable metric current-weather response.

**Architecture:** Keep the Spring MVC server from the scaffold and add Spring WebFlux only for non-blocking `WebClient` calls. A controller selects and validates the location mode, `CurrentWeatherService` composes geocoding and weather clients, provider DTOs remain internal, and a controller advice translates typed failures into stable ProblemDetail responses.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring MVC, Spring WebFlux `WebClient`, Reactor, Springdoc OpenAPI 3.1.1, JUnit 5, Mockito, Reactor Test, MockWebServer 4.12.0

## Global Constraints

- Complete `docs/superpowers/plans/2026-09-10-spring-boot-service-scaffold.md` before starting this plan.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Keep Java 21, Spring Boot 4.1.1, Springdoc OpenAPI 3.1.1, and Maven Wrapper 3.9.16.
- Keep Spring MVC as the server runtime; add WebFlux only for `WebClient` and Reactor.
- Do not call `.block()`, `.blockFirst()`, `.blockLast()`, or `.subscribe()` in production code.
- Add exactly one business route: `GET /api/v1/weather/current`.
- A non-blank city takes precedence over coordinates.
- Without a city, require both latitude and longitude.
- Accept latitude only in `-90..90` and longitude only in `-180..180`.
- Use Open-Meteo's highest-ranked city match.
- Return metric units only.
- Do not add forecasts, reverse geocoding, retries, caching, persistence, authentication, or fallback weather.
- Use production base URLs `https://geocoding-api.open-meteo.com` and `https://api.open-meteo.com`.
- Use a two-second connection timeout and five-second response timeout.
- Automated tests must use local mock HTTP servers and must not call live Open-Meteo.
- Preserve existing Actuator, Swagger UI, Docker, Compose, and GitHub Actions behavior.

---

## File Structure

| File | Responsibility |
|---|---|
| `pom.xml` | Adds WebClient, WebFlux test support, and MockWebServer. |
| `src/main/resources/application.yml` | Defines Open-Meteo URLs and timeout defaults. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherController.java` | Owns the public route, location-mode selection, validation, and OpenAPI operation documentation. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherResponse.java` | Defines the stable public weather response. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/LocationResponse.java` | Defines public resolved-location data. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/WeatherProblemHandler.java` | Maps typed endpoint failures to stable ProblemDetail responses. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationRequest.java` | Sealed input type for city and coordinate requests. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java` | Composes location resolution, weather lookup, response mapping, and weather descriptions. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptions.java` | Maps WMO codes to stable English descriptions. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/InvalidLocationException.java` | Signals invalid public location input. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationNotFoundException.java` | Signals an empty geocoding result. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderException.java` | Signals provider status, decoding, or payload failures. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderTimeoutException.java` | Signals provider connection or response timeout. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/GeocodingClient.java` | Provider-independent city-resolution interface. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/WeatherClient.java` | Provider-independent current-weather interface. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ResolvedLocation.java` | Internal resolved-location model. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ProviderCurrentWeather.java` | Internal provider weather model. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoProperties.java` | Typed Open-Meteo URLs and timeout configuration. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoClientConfiguration.java` | Builds separate timeout-enabled geocoding and forecast WebClients. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClient.java` | Calls and validates Open-Meteo geocoding. |
| `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClient.java` | Calls and validates Open-Meteo current weather. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java` | Verifies all agreed WMO descriptions. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java` | Verifies city/coordinate orchestration and response mapping. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClientTest.java` | Verifies geocoding requests and provider failures through MockWebServer. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClientTest.java` | Verifies forecast requests, time mapping, and provider failures through MockWebServer. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherControllerTest.java` | Verifies request validation, precedence, JSON, and ProblemDetail behavior. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/CurrentWeatherEndpointIT.java` | Verifies the complete HTTP flow with local mock providers. |
| `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java` | Changes the OpenAPI assertion from empty paths to the weather path. |
| `README.md` | Documents endpoint usage, units, errors, attribution, and configuration. |

---

### Task 1: Add Weather Domain Models and WMO Descriptions

**Files:**
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationRequest.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptions.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ResolvedLocation.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ProviderCurrentWeather.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/LocationResponse.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherResponse.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java`

**Interfaces:**
- Consumes: Java records and `OffsetDateTime`.
- Produces: `LocationRequest.City`, `LocationRequest.Coordinates`, `ResolvedLocation`, `ProviderCurrentWeather`, `LocationResponse`, `CurrentWeatherResponse`, and `WeatherCodeDescriptions.descriptionFor(int)`.

- [ ] **Step 1: Write the failing weather-code mapping test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WeatherCodeDescriptionsTest {

    @ParameterizedTest
    @MethodSource("descriptions")
    void mapsWeatherCode(int code, String description) {
        assertThat(WeatherCodeDescriptions.descriptionFor(code))
                .isEqualTo(description);
    }

    private static Stream<Arguments> descriptions() {
        Map<Integer, String> expected = Map.ofEntries(
                Map.entry(0, "Clear sky"),
                Map.entry(1, "Mainly clear"),
                Map.entry(2, "Partly cloudy"),
                Map.entry(3, "Overcast"),
                Map.entry(45, "Fog"),
                Map.entry(48, "Fog"),
                Map.entry(51, "Drizzle"),
                Map.entry(53, "Drizzle"),
                Map.entry(55, "Drizzle"),
                Map.entry(56, "Freezing drizzle"),
                Map.entry(57, "Freezing drizzle"),
                Map.entry(61, "Rain"),
                Map.entry(63, "Rain"),
                Map.entry(65, "Rain"),
                Map.entry(66, "Freezing rain"),
                Map.entry(67, "Freezing rain"),
                Map.entry(71, "Snowfall"),
                Map.entry(73, "Snowfall"),
                Map.entry(75, "Snowfall"),
                Map.entry(77, "Snow grains"),
                Map.entry(80, "Rain showers"),
                Map.entry(81, "Rain showers"),
                Map.entry(82, "Rain showers"),
                Map.entry(85, "Snow showers"),
                Map.entry(86, "Snow showers"),
                Map.entry(95, "Thunderstorm"),
                Map.entry(96, "Thunderstorm with hail"),
                Map.entry(99, "Thunderstorm with hail"),
                Map.entry(999, "Unknown"));

        return expected.entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }
}
```

- [ ] **Step 2: Run the mapping test to verify it fails**

Run:

```bash
./mvnw --batch-mode test -Dtest=WeatherCodeDescriptionsTest
```

Expected: FAIL because `WeatherCodeDescriptions` does not exist.

- [ ] **Step 3: Add the location request and internal provider models**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationRequest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public sealed interface LocationRequest
        permits LocationRequest.City, LocationRequest.Coordinates {

    record City(String name) implements LocationRequest {
    }

    record Coordinates(double latitude, double longitude)
            implements LocationRequest {
    }
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ResolvedLocation.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider;

public record ResolvedLocation(
        String name,
        String country,
        double latitude,
        double longitude,
        String timezone) {
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ProviderCurrentWeather.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider;

import java.time.OffsetDateTime;

public record ProviderCurrentWeather(
        double latitude,
        double longitude,
        String timezone,
        OffsetDateTime observedAt,
        int weatherCode,
        double temperatureCelsius,
        double apparentTemperatureCelsius,
        int relativeHumidityPercent,
        double precipitationMillimetres,
        double windSpeedKilometresPerHour,
        int windDirectionDegrees) {
}
```

- [ ] **Step 4: Add the public response records**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/LocationResponse.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocationResponse(
        @Schema(nullable = true, example = "Berlin")
        String name,
        @Schema(nullable = true, example = "Germany")
        String country,
        @Schema(example = "52.52")
        double latitude,
        @Schema(example = "13.41")
        double longitude,
        @Schema(example = "Europe/Berlin")
        String timezone) {
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherResponse.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.api;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record CurrentWeatherResponse(
        LocationResponse location,
        @Schema(example = "2026-09-10T18:00:00+02:00")
        OffsetDateTime observedAt,
        @Schema(example = "3")
        int weatherCode,
        @Schema(example = "Overcast")
        String description,
        @Schema(example = "18.4")
        double temperatureCelsius,
        @Schema(example = "17.9")
        double apparentTemperatureCelsius,
        @Schema(example = "71")
        int relativeHumidityPercent,
        @Schema(example = "0.0")
        double precipitationMillimetres,
        @Schema(example = "12.6")
        double windSpeedKilometresPerHour,
        @Schema(example = "245")
        int windDirectionDegrees) {
}
```

- [ ] **Step 5: Implement the weather-code mapping**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptions.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public final class WeatherCodeDescriptions {

    private WeatherCodeDescriptions() {
    }

    public static String descriptionFor(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snowfall";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }
}
```

- [ ] **Step 6: Run the weather-code test**

Run:

```bash
./mvnw --batch-mode test -Dtest=WeatherCodeDescriptionsTest
```

Expected: PASS for all documented WMO codes and the unknown-code case.

- [ ] **Step 7: Commit the weather models**

```bash
git add src/main/java/de/fraunhofer/ipa/openclawtestservice/weather \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java
git commit -m "feat: add current weather domain models" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Configure WebClient and Implement Geocoding

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationNotFoundException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderTimeoutException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/GeocodingClient.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoProperties.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoClientConfiguration.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClient.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClientTest.java`

**Interfaces:**
- Consumes: `ResolvedLocation`, Spring Boot configuration properties, and a geocoding `WebClient`.
- Produces: `GeocodingClient.resolve(String): Mono<ResolvedLocation>` plus typed not-found, provider, and timeout exceptions.

- [ ] **Step 1: Add WebClient and test dependencies**

Add this property to `pom.xml`:

```xml
        <mockwebserver.version>4.12.0</mockwebserver.version>
```

Add these dependencies after the existing MVC starter:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>mockwebserver</artifactId>
            <version>${mockwebserver.version}</version>
            <scope>test</scope>
        </dependency>
```

- [ ] **Step 2: Add Open-Meteo production configuration**

Append to `src/main/resources/application.yml`:

```yaml

weather:
  open-meteo:
    geocoding-base-url: https://geocoding-api.open-meteo.com
    forecast-base-url: https://api.open-meteo.com
    connect-timeout: 2s
    response-timeout: 5s
```

- [ ] **Step 3: Write the failing geocoding client tests**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClientTest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationNotFoundException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import reactor.netty.http.client.HttpClient;

class OpenMeteoGeocodingClientTest {

    private MockWebServer server;
    private OpenMeteoGeocodingClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(100));
        client = new OpenMeteoGeocodingClient(
                WebClient.builder()
                        .baseUrl(server.url("/").toString())
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .build());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void resolvesHighestRankedCityMatch() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "results": [{
                            "name": "Berlin",
                            "country": "Germany",
                            "latitude": 52.52437,
                            "longitude": 13.41053,
                            "timezone": "Europe/Berlin"
                          }]
                        }
                        """));

        StepVerifier.create(client.resolve("Berlin Mitte"))
                .assertNext(location -> {
                    assertThat(location.name()).isEqualTo("Berlin");
                    assertThat(location.country()).isEqualTo("Germany");
                    assertThat(location.latitude()).isEqualTo(52.52437);
                    assertThat(location.longitude()).isEqualTo(13.41053);
                    assertThat(location.timezone()).isEqualTo("Europe/Berlin");
                })
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo(
                "/v1/search?name=Berlin%20Mitte&count=1&language=en&format=json");
    }

    @Test
    void reportsMissingCity() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"results\":[]}"));

        StepVerifier.create(client.resolve("Missing"))
                .expectError(LocationNotFoundException.class)
                .verify();
    }

    @Test
    void reportsProviderFailure() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"reason\":\"failure\"}"));

        StepVerifier.create(client.resolve("Berlin"))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsMalformedPayload() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{not-json"));

        StepVerifier.create(client.resolve("Berlin"))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsIncompleteLocation() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "results": [{
                            "name": "Berlin",
                            "latitude": 52.52,
                            "longitude": 13.41,
                            "timezone": "Europe/Berlin"
                          }]
                        }
                        """));

        StepVerifier.create(client.resolve("Berlin"))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsResponseTimeout() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"results\":[]}")
                .setBodyDelay(500, TimeUnit.MILLISECONDS));

        StepVerifier.create(client.resolve("Berlin"))
                .expectError(WeatherProviderTimeoutException.class)
                .verify();
    }
}
```

- [ ] **Step 4: Run the geocoding tests to verify they fail**

Run:

```bash
./mvnw --batch-mode test -Dtest=OpenMeteoGeocodingClientTest
```

Expected: FAIL because the geocoding client and exception types do not exist.

- [ ] **Step 5: Add typed provider exceptions and geocoding interface**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationNotFoundException.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String city) {
        super("No location found for city: " + city);
    }
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderException.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class WeatherProviderException extends RuntimeException {

    public WeatherProviderException(String message) {
        super(message);
    }

    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderTimeoutException.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class WeatherProviderTimeoutException extends RuntimeException {

    public WeatherProviderTimeoutException(String operation, Throwable cause) {
        super("Weather provider timed out during " + operation, cause);
    }
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/GeocodingClient.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider;

import reactor.core.publisher.Mono;

public interface GeocodingClient {

    Mono<ResolvedLocation> resolve(String city);
}
```

- [ ] **Step 6: Add typed properties and timeout-enabled WebClients**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoProperties.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weather.open-meteo")
public record OpenMeteoProperties(
        URI geocodingBaseUrl,
        URI forecastBaseUrl,
        Duration connectTimeout,
        Duration responseTimeout) {
}
```

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoClientConfiguration.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(OpenMeteoProperties.class)
class OpenMeteoClientConfiguration {

    @Bean
    @Qualifier("openMeteoGeocodingWebClient")
    WebClient openMeteoGeocodingWebClient(
            WebClient.Builder builder,
            OpenMeteoProperties properties) {
        return build(builder, properties.geocodingBaseUrl().toString(), properties);
    }

    @Bean
    @Qualifier("openMeteoForecastWebClient")
    WebClient openMeteoForecastWebClient(
            WebClient.Builder builder,
            OpenMeteoProperties properties) {
        return build(builder, properties.forecastBaseUrl().toString(), properties);
    }

    private WebClient build(
            WebClient.Builder builder,
            String baseUrl,
            OpenMeteoProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return builder.clone()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

- [ ] **Step 7: Implement the Open-Meteo geocoding client**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClient.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import java.util.List;
import java.util.concurrent.TimeoutException;

import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationNotFoundException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.GeocodingClient;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ResolvedLocation;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
public class OpenMeteoGeocodingClient implements GeocodingClient {

    private final WebClient webClient;

    public OpenMeteoGeocodingClient(
            @Qualifier("openMeteoGeocodingWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ResolvedLocation> resolve(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new WeatherProviderException(
                                "Geocoding provider returned HTTP "
                                        + response.statusCode().value())))
                .bodyToMono(GeocodingResponse.class)
                .onErrorMap(
                        this::isTimeout,
                        cause -> new WeatherProviderTimeoutException(
                                "geocoding", cause))
                .onErrorMap(
                        cause -> cause instanceof DecodingException,
                        cause -> new WeatherProviderException(
                                "Geocoding provider returned malformed JSON",
                                cause))
                .flatMap(response -> firstResult(response, city));
    }

    private Mono<ResolvedLocation> firstResult(
            GeocodingResponse response,
            String city) {
        if (response == null || response.results() == null
                || response.results().isEmpty()) {
            return Mono.error(new LocationNotFoundException(city));
        }

        GeocodingResult result = response.results().getFirst();
        if (result.name() == null || result.country() == null
                || result.latitude() == null || result.longitude() == null
                || result.timezone() == null) {
            return Mono.error(new WeatherProviderException(
                    "Geocoding provider returned incomplete location data"));
        }

        return Mono.just(new ResolvedLocation(
                result.name(),
                result.country(),
                result.latitude(),
                result.longitude(),
                result.timezone()));
    }

    private boolean isTimeout(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof TimeoutException
                    || current instanceof ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return cause instanceof WebClientRequestException
                && cause.getMessage() != null
                && cause.getMessage().toLowerCase().contains("timeout");
    }

    private record GeocodingResponse(List<GeocodingResult> results) {
    }

    private record GeocodingResult(
            String name,
            String country,
            Double latitude,
            Double longitude,
            String timezone) {
    }
}
```

- [ ] **Step 8: Run the geocoding client tests**

Run:

```bash
./mvnw --batch-mode test -Dtest=OpenMeteoGeocodingClientTest
```

Expected: PASS for request construction, successful mapping, empty results,
provider status, and malformed JSON.

- [ ] **Step 9: Commit WebClient geocoding**

```bash
git add pom.xml src/main/resources/application.yml \
  src/main/java/de/fraunhofer/ipa/openclawtestservice/weather \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClientTest.java
git commit -m "feat: add Open-Meteo geocoding client" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 3: Implement Current-Weather Provider Client

**Files:**
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/WeatherClient.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClient.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClientTest.java`

**Interfaces:**
- Consumes: forecast `WebClient` and latitude/longitude.
- Produces: `WeatherClient.current(double, double): Mono<ProviderCurrentWeather>` with metric fields and offset-aware observation time.

- [ ] **Step 1: Write the failing weather-client success test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClientTest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.OffsetDateTime;

import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class OpenMeteoWeatherClientTest {

    private MockWebServer server;
    private OpenMeteoWeatherClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new OpenMeteoWeatherClient(
                WebClient.builder()
                        .baseUrl(server.url("/").toString())
                        .build());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void mapsMetricCurrentWeatherAndUtcOffset() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.419998,
                          "utc_offset_seconds": 7200,
                          "timezone": "Europe/Berlin",
                          "current": {
                            "time": "2026-09-10T18:00",
                            "temperature_2m": 18.4,
                            "relative_humidity_2m": 71,
                            "apparent_temperature": 17.9,
                            "precipitation": 0.0,
                            "weather_code": 3,
                            "wind_speed_10m": 12.6,
                            "wind_direction_10m": 245
                          }
                        }
                        """));

        StepVerifier.create(client.current(52.52, 13.41))
                .assertNext(weather -> {
                    assertThat(weather.latitude()).isEqualTo(52.52);
                    assertThat(weather.longitude()).isEqualTo(13.419998);
                    assertThat(weather.timezone()).isEqualTo("Europe/Berlin");
                    assertThat(weather.observedAt()).isEqualTo(
                            OffsetDateTime.parse("2026-09-10T18:00:00+02:00"));
                    assertThat(weather.weatherCode()).isEqualTo(3);
                    assertThat(weather.temperatureCelsius()).isEqualTo(18.4);
                    assertThat(weather.apparentTemperatureCelsius())
                            .isEqualTo(17.9);
                    assertThat(weather.relativeHumidityPercent()).isEqualTo(71);
                    assertThat(weather.precipitationMillimetres()).isZero();
                    assertThat(weather.windSpeedKilometresPerHour())
                            .isEqualTo(12.6);
                    assertThat(weather.windDirectionDegrees()).isEqualTo(245);
                })
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath())
                .startsWith("/v1/forecast?")
                .contains("latitude=52.52")
                .contains("longitude=13.41")
                .contains("timezone=auto")
                .contains("temperature_unit=celsius")
                .contains("wind_speed_unit=kmh")
                .contains("precipitation_unit=mm")
                .contains("current=temperature_2m%2Capparent_temperature%2Crelative_humidity_2m%2Cprecipitation%2Cweather_code%2Cwind_speed_10m%2Cwind_direction_10m");
    }

    @Test
    void rejectsIncompleteCurrentWeather() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.41,
                          "utc_offset_seconds": 7200,
                          "timezone": "Europe/Berlin",
                          "current": {}
                        }
                        """));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsProviderFailure() {
        server.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("{\"reason\":\"unavailable\"}"));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsMalformedPayload() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{not-json"));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderException.class)
                .verify();
    }
}
```

- [ ] **Step 2: Run the weather-client tests to verify they fail**

Run:

```bash
./mvnw --batch-mode test -Dtest=OpenMeteoWeatherClientTest
```

Expected: FAIL because `WeatherClient` and `OpenMeteoWeatherClient` do not
exist.

- [ ] **Step 3: Add the provider-independent weather interface**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/WeatherClient.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider;

import reactor.core.publisher.Mono;

public interface WeatherClient {

    Mono<ProviderCurrentWeather> current(double latitude, double longitude);
}
```

- [ ] **Step 4: Implement the Open-Meteo weather client**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClient.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.WeatherClient;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

    private static final String CURRENT_FIELDS = String.join(",",
            "temperature_2m",
            "apparent_temperature",
            "relative_humidity_2m",
            "precipitation",
            "weather_code",
            "wind_speed_10m",
            "wind_direction_10m");

    private final WebClient webClient;

    public OpenMeteoWeatherClient(
            @Qualifier("openMeteoForecastWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ProviderCurrentWeather> current(
            double latitude,
            double longitude) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("timezone", "auto")
                        .queryParam("current", CURRENT_FIELDS)
                        .queryParam("temperature_unit", "celsius")
                        .queryParam("wind_speed_unit", "kmh")
                        .queryParam("precipitation_unit", "mm")
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new WeatherProviderException(
                                "Forecast provider returned HTTP "
                                        + response.statusCode().value())))
                .bodyToMono(ForecastResponse.class)
                .onErrorMap(
                        this::isTimeout,
                        cause -> new WeatherProviderTimeoutException(
                                "current weather lookup", cause))
                .onErrorMap(
                        cause -> cause instanceof DecodingException,
                        cause -> new WeatherProviderException(
                                "Forecast provider returned malformed JSON",
                                cause))
                .map(this::toProviderWeather);
    }

    private ProviderCurrentWeather toProviderWeather(
            ForecastResponse response) {
        if (response == null
                || response.latitude() == null
                || response.longitude() == null
                || response.utcOffsetSeconds() == null
                || response.timezone() == null
                || response.current() == null
                || !response.current().complete()) {
            throw new WeatherProviderException(
                    "Forecast provider returned incomplete current weather");
        }

        CurrentConditions current = response.current();
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(response.utcOffsetSeconds());
        OffsetDateTime observedAt = LocalDateTime.parse(current.time())
                .atOffset(offset);

        return new ProviderCurrentWeather(
                response.latitude(),
                response.longitude(),
                response.timezone(),
                observedAt,
                current.weatherCode(),
                current.temperature(),
                current.apparentTemperature(),
                current.relativeHumidity(),
                current.precipitation(),
                current.windSpeed(),
                current.windDirection());
    }

    private boolean isTimeout(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof TimeoutException
                    || current instanceof ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private record ForecastResponse(
            Double latitude,
            Double longitude,
            @JsonProperty("utc_offset_seconds") Integer utcOffsetSeconds,
            String timezone,
            CurrentConditions current) {
    }

    private record CurrentConditions(
            String time,
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("relative_humidity_2m") Integer relativeHumidity,
            @JsonProperty("apparent_temperature") Double apparentTemperature,
            Double precipitation,
            @JsonProperty("weather_code") Integer weatherCode,
            @JsonProperty("wind_speed_10m") Double windSpeed,
            @JsonProperty("wind_direction_10m") Integer windDirection) {

        boolean complete() {
            return time != null
                    && temperature != null
                    && relativeHumidity != null
                    && apparentTemperature != null
                    && precipitation != null
                    && weatherCode != null
                    && windSpeed != null
                    && windDirection != null;
        }
    }
}
```

- [ ] **Step 5: Run both provider client test classes**

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=OpenMeteoGeocodingClientTest,OpenMeteoWeatherClientTest
```

Expected: PASS for both Open-Meteo clients.

- [ ] **Step 6: Commit current-weather provider integration**

```bash
git add src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClientTest.java
git commit -m "feat: add Open-Meteo current weather client" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Add Reactive Weather Orchestration

**Files:**
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java`

**Interfaces:**
- Consumes: `GeocodingClient.resolve(String)`, `WeatherClient.current(double, double)`, and `LocationRequest`.
- Produces: `CurrentWeatherService.current(LocationRequest): Mono<CurrentWeatherResponse>`.

- [ ] **Step 1: Write failing city and coordinate orchestration tests**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import de.fraunhofer.ipa.openclawtestservice.weather.provider.GeocodingClient;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ResolvedLocation;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.WeatherClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CurrentWeatherServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherClient weatherClient;

    private CurrentWeatherService service;

    @BeforeEach
    void setUp() {
        service = new CurrentWeatherService(geocodingClient, weatherClient);
    }

    @Test
    void resolvesCityBeforeFetchingWeather() {
        ResolvedLocation berlin = new ResolvedLocation(
                "Berlin", "Germany", 52.52, 13.41, "Europe/Berlin");
        ProviderCurrentWeather weather = weather();
        when(geocodingClient.resolve("Berlin")).thenReturn(Mono.just(berlin));
        when(weatherClient.current(52.52, 13.41)).thenReturn(Mono.just(weather));

        StepVerifier.create(service.current(new LocationRequest.City("Berlin")))
                .assertNext(response -> {
                    assertThat(response.location().name()).isEqualTo("Berlin");
                    assertThat(response.location().country()).isEqualTo("Germany");
                    assertThat(response.description()).isEqualTo("Overcast");
                    assertThat(response.temperatureCelsius()).isEqualTo(18.4);
                })
                .verifyComplete();

        verify(geocodingClient).resolve("Berlin");
        verify(weatherClient).current(52.52, 13.41);
    }

    @Test
    void skipsGeocodingForCoordinates() {
        ProviderCurrentWeather weather = weather();
        when(weatherClient.current(52.52, 13.41)).thenReturn(Mono.just(weather));

        StepVerifier.create(service.current(
                        new LocationRequest.Coordinates(52.52, 13.41)))
                .assertNext(response -> {
                    assertThat(response.location().name()).isNull();
                    assertThat(response.location().country()).isNull();
                    assertThat(response.location().timezone())
                            .isEqualTo("Europe/Berlin");
                })
                .verifyComplete();

        verify(geocodingClient, never()).resolve("Berlin");
        verify(weatherClient).current(52.52, 13.41);
    }

    private ProviderCurrentWeather weather() {
        return new ProviderCurrentWeather(
                52.52,
                13.419998,
                "Europe/Berlin",
                OffsetDateTime.parse("2026-09-10T18:00:00+02:00"),
                3,
                18.4,
                17.9,
                71,
                0.0,
                12.6,
                245);
    }
}
```

- [ ] **Step 2: Run the service tests to verify they fail**

Run:

```bash
./mvnw --batch-mode test -Dtest=CurrentWeatherServiceTest
```

Expected: FAIL because `CurrentWeatherService` does not exist.

- [ ] **Step 3: Implement reactive weather orchestration**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

import de.fraunhofer.ipa.openclawtestservice.weather.api.CurrentWeatherResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.api.LocationResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.GeocodingClient;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ResolvedLocation;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.WeatherClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CurrentWeatherService {

    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;

    public CurrentWeatherService(
            GeocodingClient geocodingClient,
            WeatherClient weatherClient) {
        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
    }

    public Mono<CurrentWeatherResponse> current(LocationRequest request) {
        return switch (request) {
            case LocationRequest.City city -> geocodingClient.resolve(city.name())
                    .flatMap(location -> weatherClient
                            .current(location.latitude(), location.longitude())
                            .map(weather -> toResponse(location, weather)));
            case LocationRequest.Coordinates coordinates -> weatherClient
                    .current(coordinates.latitude(), coordinates.longitude())
                    .map(weather -> toResponse(null, weather));
        };
    }

    private CurrentWeatherResponse toResponse(
            ResolvedLocation resolved,
            ProviderCurrentWeather weather) {
        LocationResponse location = new LocationResponse(
                resolved == null ? null : resolved.name(),
                resolved == null ? null : resolved.country(),
                weather.latitude(),
                weather.longitude(),
                weather.timezone());

        return new CurrentWeatherResponse(
                location,
                weather.observedAt(),
                weather.weatherCode(),
                WeatherCodeDescriptions.descriptionFor(weather.weatherCode()),
                weather.temperatureCelsius(),
                weather.apparentTemperatureCelsius(),
                weather.relativeHumidityPercent(),
                weather.precipitationMillimetres(),
                weather.windSpeedKilometresPerHour(),
                weather.windDirectionDegrees());
    }
}
```

- [ ] **Step 4: Run the service and weather-code tests**

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=CurrentWeatherServiceTest,WeatherCodeDescriptionsTest
```

Expected: PASS with city resolution, coordinate bypass, response mapping, and
weather descriptions verified.

- [ ] **Step 5: Commit weather orchestration**

```bash
git add src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java
git commit -m "feat: orchestrate current weather lookup" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 5: Expose the Endpoint and ProblemDetail Contract

**Files:**
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/InvalidLocationException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherController.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/WeatherProblemHandler.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherControllerTest.java`

**Interfaces:**
- Consumes: query strings `city`, `latitude`, `longitude`; `CurrentWeatherService.current(LocationRequest)`.
- Produces: `GET /api/v1/weather/current`, city precedence, numeric/range validation, and stable `400/404/502/504` ProblemDetail responses.

- [ ] **Step 1: Write the failing controller contract tests**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherControllerTest.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import de.fraunhofer.ipa.openclawtestservice.weather.application.CurrentWeatherService;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationNotFoundException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CurrentWeatherControllerTest {

    @Mock
    private CurrentWeatherService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CurrentWeatherController(service))
                .setControllerAdvice(new WeatherProblemHandler())
                .build();
    }

    @Test
    void cityTakesPrecedenceOverCoordinates() throws Exception {
        when(service.current(any())).thenReturn(Mono.just(response()));

        MvcResult pending = mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("city", " Berlin ")
                        .queryParam("latitude", "invalid")
                        .queryParam("longitude", "invalid"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.name").value("Berlin"))
                .andExpect(jsonPath("$.description").value("Overcast"));

        ArgumentCaptor<LocationRequest> request =
                ArgumentCaptor.forClass(LocationRequest.class);
        verify(service).current(request.capture());
        org.assertj.core.api.Assertions.assertThat(request.getValue())
                .isEqualTo(new LocationRequest.City("Berlin"));
    }

    @Test
    void acceptsCoordinates() throws Exception {
        when(service.current(any())).thenReturn(Mono.just(response()));

        MvcResult pending = mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("latitude", "52.52")
                        .queryParam("longitude", "13.41"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk());

        verify(service).current(
                new LocationRequest.Coordinates(52.52, 13.41));
    }

    @Test
    void rejectsIncompleteCoordinates() throws Exception {
        mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("latitude", "52.52"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type")
                        .value("urn:openclaw:error:invalid-location"));
    }

    @Test
    void rejectsBlankCityWithoutCoordinates() throws Exception {
        mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("city", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type")
                        .value("urn:openclaw:error:invalid-location"));
    }

    @Test
    void rejectsNonNumericCoordinates() throws Exception {
        mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("latitude", "north")
                        .queryParam("longitude", "13.41"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("latitude must be numeric"));
    }

    @Test
    void rejectsOutOfRangeCoordinates() throws Exception {
        mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("latitude", "91")
                        .queryParam("longitude", "13.41"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mapsMissingCityToNotFound() throws Exception {
        when(service.current(any())).thenReturn(
                Mono.error(new LocationNotFoundException("Missing")));

        MvcResult pending = mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("city", "Missing"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type")
                        .value("urn:openclaw:error:location-not-found"));
    }

    private CurrentWeatherResponse response() {
        return new CurrentWeatherResponse(
                new LocationResponse(
                        "Berlin",
                        "Germany",
                        52.52,
                        13.41,
                        "Europe/Berlin"),
                OffsetDateTime.parse("2026-09-10T18:00:00+02:00"),
                3,
                "Overcast",
                18.4,
                17.9,
                71,
                0.0,
                12.6,
                245);
    }
}
```

- [ ] **Step 2: Run the controller tests to verify they fail**

Run:

```bash
./mvnw --batch-mode test -Dtest=CurrentWeatherControllerTest
```

Expected: FAIL because the controller, invalid-location exception, and problem
handler do not exist.

- [ ] **Step 3: Add the invalid-location exception**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/InvalidLocationException.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class InvalidLocationException extends RuntimeException {

    public InvalidLocationException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Implement the controller and OpenAPI operation**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherController.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.api;

import de.fraunhofer.ipa.openclawtestservice.weather.application.CurrentWeatherService;
import de.fraunhofer.ipa.openclawtestservice.weather.application.InvalidLocationException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/weather")
public class CurrentWeatherController {

    private final CurrentWeatherService service;

    public CurrentWeatherController(CurrentWeatherService service) {
        this.service = service;
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current weather",
            description = """
                    Returns metric current weather for a city or coordinates.
                    A non-blank city takes precedence over supplied coordinates.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current weather"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid location input",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "City not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "502",
                description = "Weather provider failure",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "504",
                description = "Weather provider timeout",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Mono<CurrentWeatherResponse> current(
            @Parameter(description = "City name; takes precedence when non-blank")
            @RequestParam(required = false) String city,
            @Parameter(description = "Latitude from -90 to 90")
            @RequestParam(required = false) String latitude,
            @Parameter(description = "Longitude from -180 to 180")
            @RequestParam(required = false) String longitude) {
        return service.current(locationRequest(city, latitude, longitude));
    }

    private LocationRequest locationRequest(
            String city,
            String latitude,
            String longitude) {
        if (city != null && !city.isBlank()) {
            return new LocationRequest.City(city.trim());
        }
        if (latitude == null || longitude == null) {
            throw new InvalidLocationException(
                    "Provide a non-blank city or both latitude and longitude");
        }

        double parsedLatitude = parse(latitude, "latitude");
        double parsedLongitude = parse(longitude, "longitude");
        if (parsedLatitude < -90 || parsedLatitude > 90) {
            throw new InvalidLocationException(
                    "Latitude must be between -90 and 90");
        }
        if (parsedLongitude < -180 || parsedLongitude > 180) {
            throw new InvalidLocationException(
                    "Longitude must be between -180 and 180");
        }
        return new LocationRequest.Coordinates(
                parsedLatitude,
                parsedLongitude);
    }

    private double parse(String value, String name) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new InvalidLocationException(name + " must be numeric");
        }
    }
}
```

- [ ] **Step 5: Implement stable ProblemDetail mapping**

Create
`src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/WeatherProblemHandler.java`:

```java
package de.fraunhofer.ipa.openclawtestservice.weather.api;

import java.net.URI;

import de.fraunhofer.ipa.openclawtestservice.weather.application.InvalidLocationException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationNotFoundException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice(assignableTypes = CurrentWeatherController.class)
public class WeatherProblemHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(WeatherProblemHandler.class);

    @ExceptionHandler(InvalidLocationException.class)
    ProblemDetail invalidLocation(
            InvalidLocationException exception,
            ServletWebRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "urn:openclaw:error:invalid-location",
                "Invalid location",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    ProblemDetail locationNotFound(
            LocationNotFoundException exception,
            ServletWebRequest request) {
        return problem(
                HttpStatus.NOT_FOUND,
                "urn:openclaw:error:location-not-found",
                "Location not found",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(WeatherProviderTimeoutException.class)
    ProblemDetail providerTimeout(
            WeatherProviderTimeoutException exception,
            ServletWebRequest request) {
        LOGGER.warn("Weather provider timeout: {}", exception.getMessage());
        return problem(
                HttpStatus.GATEWAY_TIMEOUT,
                "urn:openclaw:error:weather-provider-timeout",
                "Weather provider timeout",
                "The weather provider did not respond in time",
                request);
    }

    @ExceptionHandler(WeatherProviderException.class)
    ProblemDetail providerFailure(
            WeatherProviderException exception,
            ServletWebRequest request) {
        LOGGER.warn("Weather provider failure: {}", exception.getMessage());
        return problem(
                HttpStatus.BAD_GATEWAY,
                "urn:openclaw:error:weather-provider-failure",
                "Weather provider failure",
                "The weather provider could not supply current weather",
                request);
    }

    private ProblemDetail problem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            ServletWebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }
}
```

- [ ] **Step 6: Run controller tests**

Run:

```bash
./mvnw --batch-mode test -Dtest=CurrentWeatherControllerTest
```

Expected: PASS for city precedence, coordinates, invalid input, successful JSON,
and location-not-found ProblemDetail.

- [ ] **Step 7: Extend controller tests for 502 and 504**

Add these methods to `CurrentWeatherControllerTest`:

```java
    @Test
    void mapsProviderFailureToBadGateway() throws Exception {
        when(service.current(any())).thenReturn(
                Mono.error(new de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException(
                        "provider failed")));

        MvcResult pending = mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("city", "Berlin"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type")
                        .value("urn:openclaw:error:weather-provider-failure"));
    }

    @Test
    void mapsProviderTimeoutToGatewayTimeout() throws Exception {
        when(service.current(any())).thenReturn(
                Mono.error(new de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException(
                        "current weather lookup",
                        new java.util.concurrent.TimeoutException())));

        MvcResult pending = mockMvc.perform(get("/api/v1/weather/current")
                        .queryParam("city", "Berlin"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.type")
                        .value("urn:openclaw:error:weather-provider-timeout"));
    }
```

- [ ] **Step 8: Run all endpoint unit tests**

Run:

```bash
./mvnw --batch-mode test \
  -Dtest=WeatherCodeDescriptionsTest,OpenMeteoGeocodingClientTest,OpenMeteoWeatherClientTest,CurrentWeatherServiceTest,CurrentWeatherControllerTest
```

Expected: PASS for all weather endpoint unit and mock-provider tests.

- [ ] **Step 9: Commit the public endpoint**

```bash
git add src/main/java/de/fraunhofer/ipa/openclawtestservice/weather \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherControllerTest.java
git commit -m "feat: expose current weather endpoint" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 6: Add Full HTTP Integration and Update OpenAPI Verification

**Files:**
- Create: `src/test/java/de/fraunhofer/ipa/openclawtestservice/CurrentWeatherEndpointIT.java`
- Modify: `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`

**Interfaces:**
- Consumes: complete Spring application and local geocoding/forecast MockWebServers.
- Produces: deterministic end-to-end city and coordinate verification plus exactly one documented business path.

- [ ] **Step 1: Add the full endpoint integration test**

Create
`src/test/java/de/fraunhofer/ipa/openclawtestservice/CurrentWeatherEndpointIT.java`:

```java
package de.fraunhofer.ipa.openclawtestservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CurrentWeatherEndpointIT {

    private static final MockWebServer GEOCODING = new MockWebServer();
    private static final MockWebServer FORECAST = new MockWebServer();

    static {
        try {
            GEOCODING.start();
            FORECAST.start();
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @AfterAll
    static void stopServers() throws IOException {
        GEOCODING.shutdown();
        FORECAST.shutdown();
    }

    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "weather.open-meteo.geocoding-base-url",
                () -> GEOCODING.url("/").toString());
        registry.add(
                "weather.open-meteo.forecast-base-url",
                () -> FORECAST.url("/").toString());
    }

    @Test
    void returnsCurrentWeatherForCity() throws Exception {
        GEOCODING.enqueue(json("""
                {
                  "results": [{
                    "name": "Berlin",
                    "country": "Germany",
                    "latitude": 52.52,
                    "longitude": 13.41,
                    "timezone": "Europe/Berlin"
                  }]
                }
                """));
        FORECAST.enqueue(json(forecast()));

        HttpResponse<String> response = get(
                "/api/v1/weather/current?city=Berlin");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"name\":\"Berlin\"")
                .contains("\"country\":\"Germany\"")
                .contains("\"description\":\"Overcast\"")
                .contains("\"temperatureCelsius\":18.4");
    }

    @Test
    void returnsCurrentWeatherForCoordinatesWithoutGeocoding() throws Exception {
        int geocodingRequestsBefore = GEOCODING.getRequestCount();
        FORECAST.enqueue(json(forecast()));

        HttpResponse<String> response = get(
                "/api/v1/weather/current?latitude=52.52&longitude=13.41");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"name\":null")
                .contains("\"country\":null")
                .contains("\"timezone\":\"Europe/Berlin\"");
        assertThat(GEOCODING.getRequestCount())
                .isEqualTo(geocodingRequestsBefore);
    }

    @Test
    void rejectsInvalidLocationInput() throws Exception {
        HttpResponse<String> response = get(
                "/api/v1/weather/current?latitude=91&longitude=13.41");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .startsWith("application/problem+json");
        assertThat(response.body())
                .contains("urn:openclaw:error:invalid-location");
    }

    @Test
    void mapsForecastFailureToBadGateway() throws Exception {
        FORECAST.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("{\"reason\":\"unavailable\"}"));

        HttpResponse<String> response = get(
                "/api/v1/weather/current?latitude=52.52&longitude=13.41");

        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.body())
                .contains("urn:openclaw:error:weather-provider-failure");
    }

    private HttpResponse<String> get(String path)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static String forecast() {
        return """
                {
                  "latitude": 52.52,
                  "longitude": 13.41,
                  "utc_offset_seconds": 7200,
                  "timezone": "Europe/Berlin",
                  "current": {
                    "time": "2026-09-10T18:00",
                    "temperature_2m": 18.4,
                    "relative_humidity_2m": 71,
                    "apparent_temperature": 17.9,
                    "precipitation": 0.0,
                    "weather_code": 3,
                    "wind_speed_10m": 12.6,
                    "wind_direction_10m": 245
                  }
                }
                """;
    }
}
```

- [ ] **Step 2: Update the OpenAPI path assertion**

In
`src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`,
add these imports:

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

Add this field:

```java
    private final ObjectMapper objectMapper = new ObjectMapper();
```

Replace the empty-path assertion with:

```java
        JsonNode document = objectMapper.readTree(response.body());
        assertThat(document.path("paths").fieldNames())
                .toIterable()
                .containsExactly("/api/v1/weather/current");
```

- [ ] **Step 3: Run the complete Maven verification**

Run:

```bash
./mvnw --batch-mode verify
```

Expected: PASS for startup, Actuator health, OpenAPI, endpoint integration, and
all weather unit/provider tests. No request reaches live Open-Meteo.

- [ ] **Step 4: Commit endpoint integration coverage**

```bash
git add src/test/java/de/fraunhofer/ipa/openclawtestservice/CurrentWeatherEndpointIT.java \
  src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java
git commit -m "test: verify current weather HTTP flow" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 7: Document and Validate the Delivered Endpoint

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the delivered route, configuration, units, errors, and Open-Meteo provider behavior.
- Produces: operator-facing endpoint and configuration documentation.

- [ ] **Step 1: Add endpoint documentation to the README**

Add this section after the existing service endpoint table:

````markdown
## Current Weather API

Get current weather by city:

```bash
curl --get http://localhost:8080/api/v1/weather/current \
  --data-urlencode "city=Berlin"
```

Get current weather by coordinates:

```bash
curl --get http://localhost:8080/api/v1/weather/current \
  --data-urlencode "latitude=52.52" \
  --data-urlencode "longitude=13.41"
```

When a non-blank `city` and coordinates are both supplied, the city takes
precedence. Without a city, both coordinates are required. Latitude must be
between `-90` and `90`; longitude must be between `-180` and `180`.

Example response:

```json
{
  "location": {
    "name": "Berlin",
    "country": "Germany",
    "latitude": 52.52,
    "longitude": 13.41,
    "timezone": "Europe/Berlin"
  },
  "observedAt": "2026-09-10T18:00:00+02:00",
  "weatherCode": 3,
  "description": "Overcast",
  "temperatureCelsius": 18.4,
  "apparentTemperatureCelsius": 17.9,
  "relativeHumidityPercent": 71,
  "precipitationMillimetres": 0.0,
  "windSpeedKilometresPerHour": 12.6,
  "windDirectionDegrees": 245
}
```

All values use metric units: degrees Celsius, millimetres, kilometres per hour,
degrees, and percent.

Errors use `application/problem+json`:

| Status | Meaning |
|---|---|
| `400` | Invalid or incomplete location input |
| `404` | No city match |
| `502` | Open-Meteo returned an error or invalid payload |
| `504` | Open-Meteo connection or response timeout |

Weather and geocoding data is provided by
[Open-Meteo](https://open-meteo.com/).

Provider configuration:

```yaml
weather:
  open-meteo:
    geocoding-base-url: https://geocoding-api.open-meteo.com
    forecast-base-url: https://api.open-meteo.com
    connect-timeout: 2s
    response-timeout: 5s
```
````

- [ ] **Step 2: Verify no blocking production calls were introduced**

Run:

```bash
if grep -R --line-number --include='*.java' \
  -E '\.(block|blockFirst|blockLast|subscribe)\(' src/main/java; then
  echo "Blocking or manual subscription found in production code" >&2
  exit 1
fi
```

Expected: PASS with no matches.

- [ ] **Step 3: Run final Maven and container verification**

Run:

```bash
./mvnw --batch-mode verify
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
curl --fail --silent http://localhost:8080/v3/api-docs \
  | grep -q '"/api/v1/weather/current"'
curl --fail --silent http://localhost:8080/swagger-ui/index.html \
  | grep -qi swagger

docker compose down
```

Expected: Maven verification passes; the container is healthy; OpenAPI contains
the weather path; Swagger UI remains reachable. Do not call the weather
endpoint during this container smoke test because it would use the production
Open-Meteo URLs.

- [ ] **Step 4: Verify repository hygiene**

Run:

```bash
git diff --check
git status --short
```

Expected: `git diff --check` prints nothing and `git status --short` lists only
the intended README change before the commit.

- [ ] **Step 5: Commit endpoint documentation**

```bash
git add README.md
git commit -m "docs: document current weather endpoint" \
  -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

- [ ] **Step 6: Confirm the endpoint commit set**

Run:

```bash
git status --short
git --no-pager log --oneline -7
```

Expected: the worktree is clean and these seven endpoint commits appear in
reverse order:

```text
docs: document current weather endpoint
test: verify current weather HTTP flow
feat: expose current weather endpoint
feat: orchestrate current weather lookup
feat: add Open-Meteo current weather client
feat: add Open-Meteo geocoding client
feat: add current weather domain models
```
