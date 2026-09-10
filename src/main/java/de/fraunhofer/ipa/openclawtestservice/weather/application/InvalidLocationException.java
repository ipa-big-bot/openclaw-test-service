package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class InvalidLocationException extends RuntimeException {

    public InvalidLocationException(String message) {
        super(message);
    }
}
