# Weather App

Test service for current weather endpoint.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /actuator/health | Health check |
| GET | /v3/api-docs | OpenAPI documentation |
| GET | /swagger-ui/index.html | Swagger UI |

## Current Weather API

Get current weather by city:

```bash
curl --get http://localhost:8080/api/v1/weather/current \
  --data-urlencode "city=Berlin"
```

Get current weather by coordinates:

```bash
curl --get http://localhost:8080/api/v1/weather/current \
  --data-urlencode "latitude=52.52" \
  --data-urlencode "longitude=13.41"
```

When a non-blank `city` and coordinates are both supplied, the city takes
precedence. Without a city, both coordinates are required. Latitude must be
between `-90` and `90`; longitude must be between `-180` and `180`.

Example response:

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

All values use metric units: degrees Celsius, millimetres, kilometres per hour,
degrees, and percent.

Errors use `application/problem+json`:

| Status | Meaning |
|---|---|
| `400` | Invalid or incomplete location input |
| `404` | No city match |
| `502` | Open-Meteo returned an error or invalid payload |
| `504` | Open-Meteo connection or response timeout |

Weather and geocoding data is provided by
[Open-Meteo](https://open-meteo.com/).

Provider configuration:

```yaml
weather:
  open-meteo:
    geocoding-base-url: https://geocoding-api.open-meteo.com
    forecast-base-url: https://api.open-meteo.com
    connect-timeout: 2s
    response-timeout: 5s
```

## Development

Build:

```bash
./mvnw clean package
```

Run:

```bash
./mvnw spring-boot:run
```

Test:

```bash
./mvnw test
```

Docker:

```bash
docker build -t openclaw-test-service .
docker run -p 8080:8080 openclaw-test-service
```

Docker Compose:

```bash
docker compose up
```

---

*Data provided by Open-Meteo*
