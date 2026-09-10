package de.fraunhofer.ipa.openclawtestservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void openApiDocumentContainsMetadataAndWeatherPath()
            throws IOException, InterruptedException {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"openapi\":")
                .contains("\"title\":\"OpenClaw Test Service API\"")
                .contains("\"version\":\"v1\"");

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode paths = json.get("paths");

        assertThat(paths).isNotNull();
        assertThat(paths.has("/api/v1/weather/current")).isTrue();
    }

    @Test
    void swaggerUiIsAccessible() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/swagger-ui/index.html");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).containsIgnoringCase("swagger");
    }

    private HttpResponse<String> get(String path)
            throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:" + port + path);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
