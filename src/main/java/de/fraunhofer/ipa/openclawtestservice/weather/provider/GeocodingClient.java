package de.fraunhofer.ipa.openclawtestservice.weather.provider;

import reactor.core.publisher.Mono;

public interface GeocodingClient {

    Mono<ResolvedLocation> resolve(String city);
}
