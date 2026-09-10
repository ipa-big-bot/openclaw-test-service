package de.fraunhofer.ipa.openclawtestservice.weather.provider.openmeteo;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weather.open-meteo")
public record OpenMeteoProperties(
        URI geocodingBaseUrl,
        URI forecastBaseUrl,
        Duration connectTimeout,
        Duration responseTimeout) {
}
