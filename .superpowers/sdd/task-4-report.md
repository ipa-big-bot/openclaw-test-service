# Task 4 Report: Add Reactive Weather Orchestration

## Status
DONE

## Commits
`df8f966` - Task 4: Add reactive weather orchestration (CurrentWeatherService)

## Tests
2 tests, 100% pass rate

### Test Summary:
- `resolvesCityBeforeFetchingWeather`: Verifies city-based orchestration calls geocoding then weather
- `skipsGeocodingForCoordinates`: Verifies coordinate-based lookup skips geocoding

### Test Command:
```bash
./mvnw --batch-mode test -Dtest=CurrentWeatherServiceTest
```

### Results:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

## Implementation Details

### Files Created:
1. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java`
2. `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java`

### Key Design Decisions:
- Service method `getWeather(LocationRequest): Mono<CurrentWeatherResponse>` composes geocoding and weather clients
- Uses Java 21 pattern matching for `instanceof` to handle `City` and `Coordinates` variants
- For city requests: geocoding resolves to `ResolvedLocation`, then weather lookup with full location data
- For coordinate requests: direct weather lookup without geocoding, location response has null name/country
- Supports both test scenarios via mocking of `GeocodingClient` and `WeatherClient`

### Integration Points:
- Consumes: `GeocodingClient`, `WeatherClient`, `LocationNotFoundException`, `WeatherProviderException`
- Produces: `CurrentWeatherService.getWeather(LocationRequest): Mono<CurrentWeatherResponse>`

### Constraints Met:
- No `.block()`, `.blockFirst()`, `.blockLast()`, or `.subscribe()` in production code
- Reactive chain uses `Mono.flatMap()` for composition
- Uses Spring `@Service` annotation for dependency injection
- Preserve Actuator, Swagger UI, Docker, Compose behavior

## Concerns
None
