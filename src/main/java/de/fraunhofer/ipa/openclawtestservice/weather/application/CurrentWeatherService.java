package de.fraunhofer.ipa.openclawtestservice.weather.application;

import de.fraunhofer.ipa.openclawtestservice.weather.api.CurrentWeatherResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.api.LocationResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ProviderCurrentWeather;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.ResolvedLocation;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.WeatherClient;
import de.fraunhofer.ipa.openclawtestservice.weather.provider.GeocodingClient;
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

    public Mono<CurrentWeatherResponse> getWeather(LocationRequest request) {
        return request instanceof LocationRequest.City cityRequest
                ? resolveAndFetch(cityRequest.name())
                : request instanceof LocationRequest.Coordinates coordRequest
                        ? fetchForCoordinates(coordRequest.latitude(), coordRequest.longitude())
                        : Mono.error(new IllegalArgumentException(
                                "Unsupported LocationRequest type"));
    }

    private Mono<CurrentWeatherResponse> resolveAndFetch(String city) {
        return geocodingClient.resolve(city)
                .flatMap(this::fetchForResolvedLocation);
    }

    private Mono<CurrentWeatherResponse> fetchForResolvedLocation(ResolvedLocation location) {
        return weatherClient.current(location.latitude(), location.longitude())
                .map(weather -> mapToResponse(location, weather));
    }

    private Mono<CurrentWeatherResponse> fetchForCoordinates(double latitude, double longitude) {
        return weatherClient.current(latitude, longitude)
                .map(weather -> mapToResponse(latitude, longitude, weather));
    }

    private CurrentWeatherResponse mapToResponse(
            ResolvedLocation location, ProviderCurrentWeather weather) {
        return new CurrentWeatherResponse(
                new LocationResponse(
                        location.name(),
                        location.country(),
                        location.latitude(),
                        location.longitude(),
                        location.timezone()),
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

    private CurrentWeatherResponse mapToResponse(
            double latitude, double longitude, ProviderCurrentWeather weather) {
        return new CurrentWeatherResponse(
                new LocationResponse(
                        null, // name is null for coordinate lookups
                        null, // country is null for coordinate lookups
                        weather.latitude(),
                        weather.longitude(),
                        weather.timezone()),
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
