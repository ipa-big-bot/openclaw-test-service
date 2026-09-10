package de.fraunhofer.ipa.openclawtestservice.weather.application;

public class WeatherProviderTimeoutException extends RuntimeException {

    public WeatherProviderTimeoutException(String operation, Throwable cause) {
        super("Weather provider timed out during " + operation, cause);
    }
}
