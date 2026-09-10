package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;

import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
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

class OpenMeteoWeatherClientTest {

    private MockWebServer server;
    private OpenMeteoWeatherClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(100));
        client = new OpenMeteoWeatherClient(
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
    void returnsCurrentWeather() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.41,
                          "timezone": "Europe/Berlin",
                          "current_time": "2026-09-10T12:00+02:00",
                          "current": {
                            "weather_code": 1,
                            "temperature_2m": 22.5,
                            "apparent_temperature": 23.1,
                            "relative_humidity_2m": 65,
                            "precipitation": 0.0,
                            "wind_speed_10m": 12.3,
                            "wind_direction_10m": 270
                          }
                        }
                        """));

        StepVerifier.create(client.current(52.52, 13.41))
                .assertNext(weather -> {
                    assertThat(weather.latitude()).isEqualTo(52.52);
                    assertThat(weather.longitude()).isEqualTo(13.41);
                    assertThat(weather.timezone()).isEqualTo("Europe/Berlin");
                    assertThat(weather.weatherCode()).isEqualTo(1);
                    assertThat(weather.temperatureCelsius()).isEqualTo(22.5);
                    assertThat(weather.apparentTemperatureCelsius()).isEqualTo(23.1);
                    assertThat(weather.relativeHumidityPercent()).isEqualTo(65);
                    assertThat(weather.precipitationMillimetres()).isEqualTo(0.0);
                    assertThat(weather.windSpeedKilometresPerHour()).isEqualTo(12.3);
                    assertThat(weather.windDirectionDegrees()).isEqualTo(270);
                })
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).startsWith("/v1/forecast?");
        assertThat(request.getPath()).contains("latitude=52.52");
        assertThat(request.getPath()).contains("longitude=13.41");
        assertThat(request.getPath()).contains("current=weather_code,temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,wind_speed_10m,wind_direction_10m");
    }

    @Test
    void reportsProviderFailure() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"reason\":\"failure\"}"));

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

    @Test
    void reportsIncompleteWeatherData() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.41,
                          "timezone": "Europe/Berlin",
                          "current_time": "2026-09-10T12:00+02:00",
                          "current": {
                            "weather_code": 1,
                            "temperature_2m": 22.5
                          }
                        }
                        """));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsMissingCurrentField() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.41,
                          "timezone": "Europe/Berlin",
                          "current_time": "2026-09-10T12:00+02:00"
                        }
                        """));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void reportsResponseTimeout() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 52.52,
                          "longitude": 13.41,
                          "timezone": "Europe/Berlin",
                          "current_time": "2026-09-10T12:00+02:00",
                          "current": {
                            "weather_code": 1,
                            "temperature_2m": 22.5,
                            "apparent_temperature": 23.1,
                            "relative_humidity_2m": 65,
                            "precipitation": 0.0,
                            "wind_speed_10m": 12.3,
                            "wind_direction_10m": 270
                          }
                        }
                        """)
                .setBodyDelay(500, java.util.concurrent.TimeUnit.MILLISECONDS));

        StepVerifier.create(client.current(52.52, 13.41))
                .expectError(WeatherProviderTimeoutException.class)
                .verify();
    }

    @Test
    void validatesZeroValueWindSpeed() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "latitude": 48.13,
                          "longitude": 11.57,
                          "timezone": "Europe/Berlin",
                          "current_time": "2026-09-10T12:00+02:00",
                          "current": {
                            "weather_code": 0,
                            "temperature_2m": 18.0,
                            "apparent_temperature": 17.5,
                            "relative_humidity_2m": 72,
                            "precipitation": 0.0,
                            "wind_speed_10m": 0.0,
                            "wind_direction_10m": 0
                          }
                        }
                        """));

        StepVerifier.create(client.current(48.13, 11.57))
                .assertNext(weather -> {
                    assertThat(weather.windSpeedKilometresPerHour()).isEqualTo(0.0);
                    assertThat(weather.windDirectionDegrees()).isEqualTo(0);
                })
                .verifyComplete();
    }
}
