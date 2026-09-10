package de.fraunhofer.ipa.openclawtestservice;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = de.fraunhofer.ipa.openclawtestservice.OpenclawTestServiceApplication.class)
public class CurrentWeatherEndpointIT {

    private static final MockWebServer GEOCODING = new MockWebServer();
    private static final MockWebServer FORECAST = new MockWebServer();

    @BeforeAll
    static void startServers() throws IOException {
        GEOCODING.start();
        FORECAST.start();
    }

    @AfterAll
    static void stopServers() throws IOException {
        GEOCODING.shutdown();
        FORECAST.shutdown();
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

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

        HttpResponse<String> response = get("/api/v1/weather/current?city=Berlin");

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

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private MockResponse json(String body) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private String forecast() {
        return """
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
                """;
    }

    @TestConfiguration
    static class WebFluxTestConfiguration {

        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }
}
