package de.fraunhofer.ipa.openclawtestservice.weather.provider;

import reactor.core.publisher.Mono;

public interface WeatherClient {

    Mono<ProviderCurrentWeather> current(double latitude, double longitude);
}
