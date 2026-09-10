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
