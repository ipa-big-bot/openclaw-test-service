package de.fraunhofer.ipa.openclawtestservice.weather.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocationResponse(
        @Schema(nullable = true, example = "Berlin")
        String name,
        @Schema(nullable = true, example = "Germany")
        String country,
        @Schema(example = "52.52")
        double latitude,
        @Schema(example = "13.41")
        double longitude,
        @Schema(example = "Europe/Berlin")
        String timezone) {
}
