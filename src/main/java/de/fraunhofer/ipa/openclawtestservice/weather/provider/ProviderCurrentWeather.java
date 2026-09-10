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
