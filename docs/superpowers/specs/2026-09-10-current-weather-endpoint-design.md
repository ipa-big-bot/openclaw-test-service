# Current Weather Endpoint Design

## Purpose

Add the first business endpoint to `openclaw-test-service`. The endpoint returns
normalized current weather from Open-Meteo for either a city or geographic
coordinates while preserving the existing service scaffold's operational,
OpenAPI, container, and CI behavior.

This specification extends
`docs/superpowers/specs/2026-09-10-spring-boot-service-scaffold-design.md`.

## Scope

Included:

- One versioned current-weather endpoint
- City lookup through Open-Meteo geocoding
- Coordinate-based current-weather lookup
- A stable service-owned response model
- Metric weather units
- Open-Meteo weather-code descriptions
- Explicit input and upstream failure responses
- Reactive external calls through Spring `WebClient`
- Deterministic tests using local mock HTTP servers
- OpenAPI and README updates

Excluded:

- Forecasts beyond current conditions
- Multiple city-match selection
- Reverse geocoding for coordinate requests
- Imperial units
- API keys, user accounts, persistence, caching, retries, or fallback data
- Live Open-Meteo calls from automated tests
- Additional business endpoints

## Public API Contract

### Endpoint

```text
GET /api/v1/weather/current
```

The endpoint accepts two location modes:

1. A non-blank `city` query parameter.
2. Both `latitude` and `longitude` query parameters.

If a non-blank `city` is present, it takes precedence and any supplied
coordinates are ignored. Without a city, both coordinates are required.

Examples:

```text
GET /api/v1/weather/current?city=Berlin
GET /api/v1/weather/current?latitude=52.52&longitude=13.41
```

### Input Validation

- `city` is trimmed and must contain at least one non-whitespace character to
  select city mode.
- Latitude must be numeric and in the inclusive range `-90..90`.
- Longitude must be numeric and in the inclusive range `-180..180`.
- Coordinate mode requires both latitude and longitude.
- Requests with no usable city and no complete coordinate pair return HTTP
  `400`.
- Non-numeric, partial, or out-of-range coordinate input returns HTTP `400`.
- Valid coordinates are ignored when a non-blank city is also present.

### Success Response

A successful request returns HTTP `200` and a service-owned JSON response:

```json
{
  "location": {
    "name": "Berlin",
    "country": "Germany",
    "latitude": 52.52,
    "longitude": 13.41,
    "timezone": "Europe/Berlin"
  },
  "observedAt": "2026-09-10T18:00:00+02:00",
  "weatherCode": 3,
  "description": "Overcast",
  "temperatureCelsius": 18.4,
  "apparentTemperatureCelsius": 17.9,
  "relativeHumidityPercent": 71,
  "precipitationMillimetres": 0.0,
  "windSpeedKilometresPerHour": 12.6,
  "windDirectionDegrees": 245
}
```

All weather measurements use metric units:

- Temperature and apparent temperature: degrees Celsius
- Precipitation: millimetres
- Wind speed: kilometres per hour
- Wind direction: degrees
- Relative humidity: percent

For city requests, `location.name` and `location.country` come from the
highest-ranked Open-Meteo geocoding result. For coordinate requests, reverse
geocoding is not performed, so `location.name` and `location.country` are
`null`. Both modes return the actual coordinates and timezone used for the
weather observation.

`observedAt` is an ISO 8601 offset date-time constructed from the observation
time in the timezone returned by Open-Meteo.

## Architecture and Components

The endpoint uses focused controller, service, and provider-client boundaries.

### Weather Controller

`CurrentWeatherController` owns the public HTTP route, query-parameter parsing,
validation, OpenAPI annotations, and response status behavior. It converts
valid query parameters into one of two explicit location requests:

- `CityLocationRequest`
- `CoordinateLocationRequest`

The controller returns `Mono<CurrentWeatherResponse>` without blocking.

### Current Weather Service

`CurrentWeatherService` owns application orchestration:

- Applies city precedence
- Resolves a city through the geocoding client
- Bypasses geocoding for coordinate requests
- Requests current conditions for resolved coordinates
- Maps provider data into the public response
- Converts WMO weather codes into stable descriptions

The service depends on provider clients through small interfaces so orchestration
tests do not need HTTP.

### Open-Meteo Geocoding Client

`OpenMeteoGeocodingClient` calls the Open-Meteo geocoding API using a dedicated
`WebClient`. It URL-encodes the city and requests one result. The first returned
result supplies:

- Resolved name
- Country
- Latitude
- Longitude
- Timezone

An empty results list maps to the service's city-not-found error.

### Open-Meteo Weather Client

`OpenMeteoWeatherClient` calls the Open-Meteo forecast API with:

- Latitude and longitude
- `timezone=auto`
- Current temperature
- Apparent temperature
- Relative humidity
- Precipitation
- Weather code
- Wind speed
- Wind direction
- Explicit metric units

It maps provider JSON into an internal current-weather model. Provider DTOs are
not returned from the controller.

### WebClient Configuration

The application remains a Spring MVC servlet application. Spring WebFlux is
added for `WebClient` and Reactor only; it does not replace the MVC server.

Two configured `WebClient` instances isolate geocoding and forecast base URLs.
Typed properties under `weather.open-meteo` define:

- Geocoding base URL
- Forecast base URL
- Connection timeout
- Response timeout

Production defaults target Open-Meteo. Tests replace both URLs with local mock
HTTP server addresses. Reactor Netty enforces finite connection and response
timeouts.

## Data Flow

### City Request

1. The controller receives and trims `city`.
2. The controller selects city mode even if coordinates are also present.
3. The service requests the highest-ranked geocoding match.
4. The geocoding client returns a resolved location or a city-not-found error.
5. The service requests current weather for the resolved coordinates.
6. The weather client maps the Open-Meteo response into an internal model.
7. The service combines resolved location and weather data into the public
   response.
8. The controller returns HTTP `200`.

### Coordinate Request

1. The controller validates latitude and longitude.
2. The service skips geocoding.
3. The weather client requests current weather with `timezone=auto`.
4. The service creates a location with `null` name and country, preserving the
   coordinates and provider-resolved timezone.
5. The service maps current conditions into the public response.
6. The controller returns HTTP `200`.

## Weather Code Mapping

The service maps the WMO codes documented by Open-Meteo into stable English
descriptions:

| Codes | Description |
|---|---|
| `0` | Clear sky |
| `1` | Mainly clear |
| `2` | Partly cloudy |
| `3` | Overcast |
| `45`, `48` | Fog |
| `51`, `53`, `55` | Drizzle |
| `56`, `57` | Freezing drizzle |
| `61`, `63`, `65` | Rain |
| `66`, `67` | Freezing rain |
| `71`, `73`, `75` | Snowfall |
| `77` | Snow grains |
| `80`, `81`, `82` | Rain showers |
| `85`, `86` | Snow showers |
| `95` | Thunderstorm |
| `96`, `99` | Thunderstorm with hail |

An unknown future numeric code remains a successful response with description
`"Unknown"` and the original code preserved.

## Error Contract

Errors use Spring `ProblemDetail` and media type `application/problem+json`.
Each response contains a stable problem `type`, HTTP `status`, human-readable
`title` and `detail`, and the request path as `instance`.

| Status | Condition |
|---|---|
| `400 Bad Request` | Missing, partial, non-numeric, blank-only, or out-of-range location input |
| `404 Not Found` | Open-Meteo geocoding returns no result for the city |
| `502 Bad Gateway` | Open-Meteo returns an error response, malformed JSON, or incomplete current conditions |
| `504 Gateway Timeout` | Connection or response timeout occurs while calling Open-Meteo |

The endpoint does not retry, cache, return stale data, or fabricate successful
fallback responses. Unexpected local failures retain the framework's standard
`500` behavior.

Logs identify the upstream operation and failure category. They do not log full
provider response bodies or unnecessary query values.

## OpenAPI Documentation

Swagger UI documents:

- Route and purpose
- City mode
- Coordinate mode and ranges
- City precedence when both modes are supplied
- Metric units
- Success schema and nullable coordinate-mode location fields
- `400`, `404`, `502`, and `504` problem responses

The generated OpenAPI document contains exactly one business path:
`/api/v1/weather/current`. Actuator endpoints remain excluded from the business
API document.

## Testing

### Controller Tests

Focused MVC tests verify:

- City-only requests
- Coordinate-only requests
- City precedence over supplied coordinates
- Missing and partial location input
- Blank city handling
- Numeric and range validation
- Public response serialization
- `400`, `404`, `502`, and `504` status and ProblemDetail serialization

The service dependency is stubbed with reactive `Mono` results.

### Service Tests

Service tests use Reactor `StepVerifier` to verify:

- City resolution before weather lookup
- Highest-ranked city match use
- Geocoding bypass for coordinates
- Provider-to-public response mapping
- Nullable name and country for coordinates
- Weather-code descriptions, including unknown codes
- Propagation of typed city-not-found, upstream, and timeout errors

### Provider Client Tests

Local mock HTTP servers verify:

- Exact geocoding path and query parameters
- Exact forecast path, current-field selection, timezone, and metric units
- Successful provider response mapping
- Empty geocoding results
- Provider error statuses
- Malformed and incomplete responses
- Connection or delayed-response timeout behavior

No automated test calls live Open-Meteo.

### Application Integration Tests

A random-port Spring Boot integration test overrides both provider base URLs
with local mock HTTP servers. It exercises:

- City request through geocoding and weather lookup
- Coordinate request through weather lookup only
- Full public JSON response
- Representative input and upstream error responses

Existing application startup, Actuator health, Docker, Compose, and GitHub
Actions checks remain. The existing OpenAPI integration test changes from
expecting an empty `paths` object to expecting exactly the current-weather
operation.

## Documentation

The README adds:

- Endpoint URL
- City and coordinate request examples
- Parameter precedence and validation rules
- Example success response
- Metric-unit definitions
- Error status meanings
- Open-Meteo attribution
- Provider URL and timeout configuration

## Acceptance Criteria

- `GET /api/v1/weather/current?city=Berlin` resolves the highest-ranked
  Open-Meteo location and returns current metric weather.
- A request with valid latitude and longitude skips geocoding and returns
  current metric weather.
- A non-blank city takes precedence when coordinates are also supplied.
- Invalid or incomplete location input returns `400` ProblemDetail.
- A city with no geocoding match returns `404` ProblemDetail.
- Provider failures and malformed responses return `502` ProblemDetail.
- Provider connection or response timeouts return `504` ProblemDetail.
- The response does not expose Open-Meteo DTOs or raw field names.
- The service uses `WebClient` without blocking.
- Automated tests use local mock HTTP servers and never call live Open-Meteo.
- OpenAPI documents exactly one business path and all agreed response outcomes.
- Existing startup, Actuator, container, Compose, and CI behavior remains
  intact.
