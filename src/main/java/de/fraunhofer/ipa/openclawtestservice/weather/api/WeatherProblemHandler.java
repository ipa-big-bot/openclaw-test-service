package de.fraunhofer.ipa.openclawtestservice.weather.api;

import java.net.URI;

import de.fraunhofer.ipa.openclawtestservice.weather.application.InvalidLocationException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationNotFoundException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.WeatherProviderTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice(assignableTypes = CurrentWeatherController.class)
public class WeatherProblemHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(WeatherProblemHandler.class);

    @ExceptionHandler(InvalidLocationException.class)
    ProblemDetail invalidLocation(
            InvalidLocationException exception,
            ServletWebRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "urn:openclaw:error:invalid-location",
                "Invalid location",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    ProblemDetail locationNotFound(
            LocationNotFoundException exception,
            ServletWebRequest request) {
        return problem(
                HttpStatus.NOT_FOUND,
                "urn:openclaw:error:location-not-found",
                "Location not found",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(WeatherProviderTimeoutException.class)
    ProblemDetail providerTimeout(
            WeatherProviderTimeoutException exception,
            ServletWebRequest request) {
        LOGGER.warn("Weather provider timeout: {}", exception.getMessage());
        return problem(
                HttpStatus.GATEWAY_TIMEOUT,
                "urn:openclaw:error:weather-provider-timeout",
                "Weather provider timeout",
                "The weather provider did not respond in time",
                request);
    }

    @ExceptionHandler(WeatherProviderException.class)
    ProblemDetail providerFailure(
            WeatherProviderException exception,
            ServletWebRequest request) {
        LOGGER.warn("Weather provider failure: {}", exception.getMessage());
        return problem(
                HttpStatus.BAD_GATEWAY,
                "urn:openclaw:error:weather-provider-failure",
                "Weather provider failure",
                "The weather provider could not supply current weather",
                request);
    }

    private ProblemDetail problem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            ServletWebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequest().getRequestURI()));
        return problem;
    }
}
