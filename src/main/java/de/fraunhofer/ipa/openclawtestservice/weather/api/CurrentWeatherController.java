package de.fraunhofer.ipa.openclawtestservice.weather.api;

import de.fraunhofer.ipa.openclawtestservice.weather.application.CurrentWeatherService;
import de.fraunhofer.ipa.openclawtestservice.weather.application.InvalidLocationException;
import de.fraunhofer.ipa.openclawtestservice.weather.application.LocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/weather")
public class CurrentWeatherController {

    private final CurrentWeatherService service;

    public CurrentWeatherController(CurrentWeatherService service) {
        this.service = service;
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current weather",
            description = """
                    Returns metric current weather for a city or coordinates.
                    A non-blank city takes precedence over supplied coordinates.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current weather"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid location input",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "City not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "502",
                description = "Weather provider failure",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "504",
                description = "Weather provider timeout",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Mono<CurrentWeatherResponse> getWeather(
            @Parameter(description = "City name; takes precedence when non-blank")
            @RequestParam(required = false) String city,
            @Parameter(description = "Latitude from -90 to 90")
            @RequestParam(required = false) String latitude,
            @Parameter(description = "Longitude from -180 to 180")
            @RequestParam(required = false) String longitude) {
        return service.getWeather(locationRequest(city, latitude, longitude));
    }

    private LocationRequest locationRequest(
            String city,
            String latitude,
            String longitude) {
        if (city != null && !city.isBlank()) {
            return new LocationRequest.City(city.trim());
        }
        if (latitude == null || longitude == null) {
            throw new InvalidLocationException(
                    "Provide a non-blank city or both latitude and longitude");
        }

        double parsedLatitude = parse(latitude, "latitude");
        double parsedLongitude = parse(longitude, "longitude");
        if (parsedLatitude < -90 || parsedLatitude > 90) {
            throw new InvalidLocationException(
                    "Latitude must be between -90 and 90");
        }
        if (parsedLongitude < -180 || parsedLongitude > 180) {
            throw new InvalidLocationException(
                    "Longitude must be between -180 and 180");
        }
        return new LocationRequest.Coordinates(
                parsedLatitude,
                parsedLongitude);
    }

    private double parse(String value, String name) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new InvalidLocationException(name + " must be numeric");
        }
    }
}
