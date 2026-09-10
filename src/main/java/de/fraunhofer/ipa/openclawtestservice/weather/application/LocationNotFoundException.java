package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String city) {
        super("No location found for city: " + city);
    }
}
