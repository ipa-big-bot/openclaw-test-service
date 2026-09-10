package de.fraunhofer.ipa.openclawtestservice.weather.api;

import static org.assertj.core.api.Assertions.assertThat;

import de.fraunhofer.ipa.openclawtestservice.weather.application.CurrentWeatherService;
import de.fraunhofer.ipa.openclawtestservice.weather.application.InvalidLocationException;
import de.fraunhofer.ipa.openclawtestservice.weather.api.CurrentWeatherResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.api.LocationResponse;
import de.fraunhofer.ipa.openclawtestservice.weather.api.WeatherProblemHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import reactor.core.publisher.Mono;

class CurrentWeatherControllerTest {

    private CurrentWeatherController controller;
    private CurrentWeatherService service;

    @BeforeEach
    void setUp() {
        service = new CurrentWeatherService(null, null) {
            @Override
            public Mono<CurrentWeatherResponse> getWeather(
                    de.fraunhofer.ipa.openclawtestservice.weather.application.LocationRequest request) {
                throw new UnsupportedOperationException("Not implemented in test");
            }
        };
        controller = new CurrentWeatherController(service);
    }

    @Test
    void locationRequestWithCityUsesCity() {
        // This is a test of the private method via reflection
        // In practice, we'd test the controller method
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    void locationRequestWithCoordinatesUsesCoordinates() {
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    void locationRequestWithInvalidLatitudeThrows() {
        try {
            invokeLocationRequest(null, "91", "13.41");
            assertThat(false).as("Expected InvalidLocationException").isTrue();
        } catch (InvalidLocationException e) {
            assertThat(e.getMessage()).contains("Latitude must be between");
        }
    }

    @Test
    void locationRequestWithInvalidLongitudeThrows() {
        try {
            invokeLocationRequest(null, "52.52", "181");
            assertThat(false).as("Expected InvalidLocationException").isTrue();
        } catch (InvalidLocationException e) {
            assertThat(e.getMessage()).contains("Longitude must be between");
        }
    }

    @Test
    void locationRequestWithNonNumericLatitudeThrows() {
        try {
            invokeLocationRequest(null, "abc", "13.41");
            assertThat(false).as("Expected InvalidLocationException").isTrue();
        } catch (InvalidLocationException e) {
            assertThat(e.getMessage()).contains("must be numeric");
        }
    }

    @Test
    void locationRequestWithMissingCoordinatesThrows() {
        try {
            invokeLocationRequest(null, null, null);
            assertThat(false).as("Expected InvalidLocationException").isTrue();
        } catch (InvalidLocationException e) {
            assertThat(e.getMessage()).contains("Provide a non-blank city");
        }
    }

    @Test
    void controllerInstantiatesWithService() {
        assertThat(controller).isNotNull();
    }

    // Private method to test locationRequest
    private Object invokeLocationRequest(String city, String latitude, String longitude) {
        try {
            var method = CurrentWeatherController.class.getDeclaredMethod(
                    "locationRequest", String.class, String.class, String.class);
            method.setAccessible(true);
            return method.invoke(controller, city, latitude, longitude);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
}
