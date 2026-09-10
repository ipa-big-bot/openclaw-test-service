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
