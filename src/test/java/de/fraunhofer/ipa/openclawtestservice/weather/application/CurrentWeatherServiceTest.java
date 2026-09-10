package de.fraunhofer.ipa.openclawtestservice.weather.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeoutException;

import de.fraunhofer.ipa.openclawtestservice.weather.api.CurrentWeatherResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.api.LocationResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ResolvedLocation;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.WeatherClient;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.GeocodingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CurrentWeatherServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherClient weatherClient;

    private CurrentWeatherService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CurrentWeatherService(geocodingClient, weatherClient);
    }

    @Test
    void returnsWeatherForCityRequest() {
        ResolvedLocation resolvedLocation = new ResolvedLocation(
                "Berlin", "Germany", 52.52, 13.41, "Europe/Berlin");
        ProviderCurrentWeather weather = new ProviderCurrentWeather(
                52.52, 13.41, "Europe/Berlin",
                OffsetDateTime.parse("2026-09-10T18:00:00+02:00"),
                3, 18.4, 17.9, 71, 0.0, 12.6, 245);

        when(geocodingClient.resolve("Berlin"))
                .thenReturn(Mono.just(resolvedLocation));
        when(weatherClient.current(52.52, 13.41))
                .thenReturn(Mono.just(weather));

        StepVerifier.create(service.getWeather(new LocationRequest.City("Berlin")))
                .assertNext(response -> {
                    assertThat(response.location()).isEqualTo(new LocationResponse(
                            "Berlin", "Germany", 52.52, 13.41, "Europe/Berlin"));
                    assertThat(response.observedAt())
                            .isEqualTo(OffsetDateTime.parse("2026-09-10T18:00:00+02:00"));
                    assertThat(response.weatherCode()).isEqualTo(3);
                    assertThat(response.description()).isEqualTo("Overcast");
                    assertThat(response.temperatureCelsius()).isEqualTo(18.4);
                    assertThat(response.apparentTemperatureCelsius()).isEqualTo(17.9);
                    assertThat(response.relativeHumidityPercent()).isEqualTo(71);
                    assertThat(response.precipitationMillimetres()).isEqualTo(0.0);
                    assertThat(response.windSpeedKilometresPerHour()).isEqualTo(12.6);
                    assertThat(response.windDirectionDegrees()).isEqualTo(245);
                })
                .verifyComplete();
    }

    @Test
    void returnsWeatherForCoordinatesRequest() {
        ProviderCurrentWeather weather = new ProviderCurrentWeather(
                48.13, 11.57, "Europe/Berlin",
                OffsetDateTime.parse("2026-09-10T12:00:00+02:00"),
                1, 22.5, 23.1, 65, 0.0, 8.2, 180);

        when(weatherClient.current(48.13, 11.57))
                .thenReturn(Mono.just(weather));

        StepVerifier.create(service.getWeather(new LocationRequest.Coordinates(48.13, 11.57)))
                .assertNext(response -> {
                    assertThat(response.location()).isEqualTo(new LocationResponse(
                            null, null, 48.13, 11.57, "Europe/Berlin"));
                    assertThat(response.observedAt())
                            .isEqualTo(OffsetDateTime.parse("2026-09-10T12:00:00+02:00"));
                    assertThat(response.weatherCode()).isEqualTo(1);
                    assertThat(response.description()).isEqualTo("Mainly clear");
                    assertThat(response.temperatureCelsius()).isEqualTo(22.5);
                    assertThat(response.apparentTemperatureCelsius()).isEqualTo(23.1);
                    assertThat(response.relativeHumidityPercent()).isEqualTo(65);
                    assertThat(response.precipitationMillimetres()).isEqualTo(0.0);
                    assertThat(response.windSpeedKilometresPerHour()).isEqualTo(8.2);
                    assertThat(response.windDirectionDegrees()).isEqualTo(180);
                })
                .verifyComplete();
    }

    @Test
    void propagatesLocationNotFoundException() {
        when(geocodingClient.resolve("NonExistentCity"))
                .thenReturn(Mono.error(new LocationNotFoundException("NonExistentCity")));

        StepVerifier.create(service.getWeather(new LocationRequest.City("NonExistentCity")))
                .expectError(LocationNotFoundException.class)
                .verify();
    }

    @Test
    void propagatesWeatherProviderException() {
        ResolvedLocation resolvedLocation = new ResolvedLocation(
                "Berlin", "Germany", 52.52, 13.41, "Europe/Berlin");

        when(geocodingClient.resolve("Berlin"))
                .thenReturn(Mono.just(resolvedLocation));
        when(weatherClient.current(52.52, 13.41))
                .thenReturn(Mono.error(new WeatherProviderException("Provider error")));

        StepVerifier.create(service.getWeather(new LocationRequest.City("Berlin")))
                .expectError(WeatherProviderException.class)
                .verify();
    }

    @Test
    void propagatesWeatherProviderTimeoutException() {
        ResolvedLocation resolvedLocation = new ResolvedLocation(
                "Berlin", "Germany", 52.52, 13.41, "Europe/Berlin");

        when(geocodingClient.resolve("Berlin"))
                .thenReturn(Mono.just(resolvedLocation));
        when(weatherClient.current(52.52, 13.41))
                .thenReturn(Mono.error(new WeatherProviderTimeoutException(
                        "forecast", new TimeoutException())));

        StepVerifier.create(service.getWeather(new LocationRequest.City("Berlin")))
                .expectError(WeatherProviderTimeoutException.class)
                .verify();
    }

    @Test
    void handlesCoordinatesWithZeroValueWindSpeed() {
        ProviderCurrentWeather weather = new ProviderCurrentWeather(
                48.13, 11.57, "Europe/Berlin",
                OffsetDateTime.parse("2026-09-10T12:00:00+02:00"),
                0, 18.0, 17.5, 72, 0.0, 0.0, 0);

        when(weatherClient.current(48.13, 11.57))
                .thenReturn(Mono.just(weather));

        StepVerifier.create(service.getWeather(new LocationRequest.Coordinates(48.13, 11.57)))
                .assertNext(response -> {
                    assertThat(response.location()).isEqualTo(new LocationResponse(
                            null, null, 48.13, 11.57, "Europe/Berlin"));
                    assertThat(response.weatherCode()).isEqualTo(0);
                    assertThat(response.description()).isEqualTo("Clear sky");
                    assertThat(response.windSpeedKilometresPerHour()).isEqualTo(0.0);
                    assertThat(response.windDirectionDegrees()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    void returnsUnknownDescriptionForUnknownWeatherCode() {
        ProviderCurrentWeather weather = new ProviderCurrentWeather(
                52.52, 13.41, "Europe/Berlin",
                OffsetDateTime.parse("2026-09-10T18:00:00+02:00"),
                999, 18.4, 17.9, 71, 0.0, 12.6, 245);

        when(weatherClient.current(52.52, 13.41))
                .thenReturn(Mono.just(weather));

        StepVerifier.create(service.getWeather(new LocationRequest.Coordinates(52.52, 13.41)))
                .assertNext(response -> {
                    assertThat(response.weatherCode()).isEqualTo(999);
                    assertThat(response.description()).isEqualTo("Unknown");
                })
                .verifyComplete();
    }
}
