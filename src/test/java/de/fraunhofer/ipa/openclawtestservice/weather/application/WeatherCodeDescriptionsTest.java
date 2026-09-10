package de.fraunhofer.ipa.openclawtestservice.weather.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WeatherCodeDescriptionsTest {

    @ParameterizedTest
    @MethodSource("descriptions")
    void mapsWeatherCode(int code, String description) {
        assertThat(WeatherCodeDescriptions.descriptionFor(code))
                .isEqualTo(description);
    }

    private static Stream<Arguments> descriptions() {
        Map<Integer, String> expected = Map.ofEntries(
                Map.entry(0, "Clear sky"),
                Map.entry(1, "Mainly clear"),
                Map.entry(2, "Partly cloudy"),
                Map.entry(3, "Overcast"),
                Map.entry(45, "Fog"),
                Map.entry(48, "Fog"),
                Map.entry(51, "Drizzle"),
                Map.entry(53, "Drizzle"),
                Map.entry(55, "Drizzle"),
                Map.entry(56, "Freezing drizzle"),
                Map.entry(57, "Freezing drizzle"),
                Map.entry(61, "Rain"),
                Map.entry(63, "Rain"),
                Map.entry(65, "Rain"),
                Map.entry(66, "Freezing rain"),
                Map.entry(67, "Freezing rain"),
                Map.entry(71, "Snowfall"),
                Map.entry(73, "Snowfall"),
                Map.entry(75, "Snowfall"),
                Map.entry(77, "Snow grains"),
                Map.entry(80, "Rain showers"),
                Map.entry(81, "Rain showers"),
                Map.entry(82, "Rain showers"),
                Map.entry(85, "Snow showers"),
                Map.entry(86, "Snow showers"),
                Map.entry(95, "Thunderstorm"),
                Map.entry(96, "Thunderstorm with hail"),
                Map.entry(99, "Thunderstorm with hail"),
                Map.entry(999, "Unknown"));

        return expected.entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }
}
