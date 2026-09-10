package de.fraunhofer.ipa.openclawtestservice.weather.api;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record CurrentWeatherResponse(
        LocationResponse location,
        @Schema(example = "2026-09-10T18:00:00+02:00")
        OffsetDateTime observedAt,
        @Schema(example = "3")
        int weatherCode,
        @Schema(example = "Overcast")
        String description,
        @Schema(example = "18.4")
        double temperatureCelsius,
        @Schema(example = "17.9")
        double apparentTemperatureCelsius,
        @Schema(example = "71")
        int relativeHumidityPercent,
        @Schema(example = "0.0")
        double precipitationMillimetres,
        @Schema(example = "12.6")
        double windSpeedKilometresPerHour,
        @Schema(example = "245")
        int windDirectionDegrees) {
}
