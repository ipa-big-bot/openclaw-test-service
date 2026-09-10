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
