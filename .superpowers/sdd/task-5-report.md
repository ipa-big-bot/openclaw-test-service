# Task 5 Report: Expose Current Weather Endpoint

**Date:** 2026-09-10  
**Task:** Task 5 from Current Weather Endpoint plan  
**Status:** ✅ Complete

## Summary

Implemented the Current Weather Endpoint (`GET /api/v1/weather/current`) with:
- REST controller for the endpoint
- ProblemDetail exception mapping
- Controller contract tests
- Integration tests with mock providers

## Files Created

| File | Path | Responsibility |
|------|------|----------------|
| `CurrentWeatherController.java` | `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherController.java` | REST controller for `/api/v1/weather/current` with OpenAPI documentation |
| `WeatherProblemHandler.java` | `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/WeatherProblemHandler.java` | ProblemDetail exception mapping for typed failures |
| `InvalidLocationException.java` | `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/InvalidLocationException.java` | Exception for invalid location input |
| `CurrentWeatherControllerTest.java` | `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherControllerTest.java` | Controller contract tests |
| `CurrentWeatherEndpointIT.java` | `src/test/java/de/fraunhofer/ipa/openclawtestservice/CurrentWeatherEndpointIT.java` | Integration tests with mock providers |

## Test Results

```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Controller Tests (`CurrentWeatherControllerTest`)
- 7 tests covering location validation:
  - `locationRequestWithCityUsesCity`
  - `locationRequestWithCoordinatesUsesCoordinates`
  - `locationRequestWithInvalidLatitudeThrows`
  - `locationRequestWithInvalidLongitudeThrows`
  - `locationRequestWithNonNumericLatitudeThrows`
  - `locationRequestWithMissingCoordinatesThrows`
  - `controllerInstantiatesWithService`

### Integration Tests (`CurrentWeatherEndpointIT`)
- 4 tests with local mock HTTP servers:
  - `returnsCurrentWeatherForCity` - 200 OK with city lookup
  - `returnsCurrentWeatherForCoordinatesWithoutGeocoding` - 200 OK for direct coordinates
  - `rejectsInvalidLocationInput` - 400 Bad Request with ProblemDetail
  - `mapsForecastFailureToBadGateway` - 502 Bad Gateway with ProblemDetail

## Implementation Notes

### ProblemDetail Contract
The `WeatherProblemHandler` maps exceptions to stable ProblemDetail responses:
- `InvalidLocationException` → 400 `urn:openclaw:error:invalid-location`
- `LocationNotFoundException` → 404 `urn:openclaw:error:location-not-found`
- `WeatherProviderTimeoutException` → 504 `urn:openclaw:error:weather-provider-timeout`
- `WeatherProviderException` → 502 `urn:openclaw:error:weather-provider-failure`

### WebClient Configuration
Added `WebClientTestConfiguration` to provide `WebClient.Builder` bean for integration tests, since the main application uses Spring MVC but WebFlux WebClient is required for non-blocking HTTP calls.

### Validation
The controller validates:
- City name takes precedence over coordinates when provided
- Both latitude and longitude are required when city is not provided
- Latitude must be between -90 and 90
- Longitude must be between -180 and 180
- Latitude and longitude must be numeric

## Commit

```bash
git add .
git commit -m "feat: add current weather endpoint (Task 5)
- CurrentWeatherController with OpenAPI documentation
- WeatherProblemHandler for stable ProblemDetail responses
- InvalidLocationException for validation failures
- Controller contract and integration tests with mock providers
- Resolves /api/v1/weather/current endpoint"
```

## Concerns

None. All tests pass and the implementation matches the task brief requirements.
