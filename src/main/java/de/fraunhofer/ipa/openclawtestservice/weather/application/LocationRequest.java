package de.fraunhofer.ipa.openclawtestservice.weather.application;

public sealed interface LocationRequest
        permits LocationRequest.City, LocationRequest.Coordinates {

    record City(String name) implements LocationRequest {
    }

    record Coordinates(double latitude, double longitude)
            implements LocationRequest {
    }
}
