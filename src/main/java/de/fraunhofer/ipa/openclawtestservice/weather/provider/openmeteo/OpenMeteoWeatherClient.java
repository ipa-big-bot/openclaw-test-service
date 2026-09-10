package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

    private final WebClient webClient;

    public OpenMeteoWeatherClient(
            @Qualifier("openMeteoForecastWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ProviderCurrentWeather> current(double latitude, double longitude) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "weather_code,temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,wind_speed_10m,wind_direction_10m")
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new WeatherProviderException(
                                "Weather provider returned HTTP "
                                        + response.statusCode().value())))
                .bodyToMono(WeatherResponse.class)
                .onErrorMap(
                        this::isTimeout,
                        cause -> new WeatherProviderTimeoutException(
                                "weather", cause))
                .onErrorMap(
                        cause -> cause instanceof DecodingException,
                        cause -> new WeatherProviderException(
                                "Weather provider returned malformed JSON",
                                cause))
                .flatMap(this::mapCurrentWeather);
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

    private Mono<ProviderCurrentWeather> mapCurrentWeather(WeatherResponse response) {
        if (response == null || response.current() == null) {
            return Mono.error(new WeatherProviderException(
                    "Weather provider returned incomplete weather data"));
        }

        CurrentWeather current = response.current();
        if (current.weather_code() == null
                || current.temperature_2m() == null
                || current.apparent_temperature() == null
                || current.relative_humidity_2m() == null
                || current.precipitation() == null
                || current.wind_speed_10m() == null
                || current.wind_direction_10m() == null) {
            return Mono.error(new WeatherProviderException(
                    "Weather provider returned incomplete weather fields"));
        }

        OffsetDateTime observedAt = response.current_time() != null
                ? OffsetDateTime.parse(response.current_time())
                : OffsetDateTime.now();

        return Mono.just(new ProviderCurrentWeather(
                response.latitude(),
                response.longitude(),
                response.timezone(),
                observedAt,
                current.weather_code(),
                current.temperature_2m(),
                current.apparent_temperature(),
                current.relative_humidity_2m(),
                current.precipitation(),
                current.wind_speed_10m(),
                current.wind_direction_10m()));
    }

    private record WeatherResponse(
            double latitude,
            double longitude,
            String timezone,
            String current_time,
            CurrentWeather current) {
    }

    private record CurrentWeather(
            Integer weather_code,
            Double temperature_2m,
            Double apparent_temperature,
            Integer relative_humidity_2m,
            Double precipitation,
            Double wind_speed_10m,
            Integer wind_direction_10m) {
    }
}
