package de.fraunhofer.ipa.openclawtestservice.weather.provider;

public record ResolvedLocation(
        String name,
        String country,
        double latitude,
        double longitude,
        String timezone) {
}
