# Task 3 Brief: Implement Current-Weather Provider Client

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/WeatherClient.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClient.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoWeatherClientTest.java`

## Interfaces
- Produces: `WeatherClient.current(double, double): Mono<ProviderCurrentWeather>` with metric fields.

## Steps
1. Write failing weather-client success test
2. Run tests to verify failure
3. Add the provider-independent weather interface
4. Implement the Open-Meteo weather client
5. Run both provider client tests
6. Commit current-weather provider integration

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 3 of the Current Weather Endpoint plan. The goal is to add Open-Meteo current-weather client.

## Global Constraints
- Use Java 21, Spring Boot 4.1.1, Spring WebFlux WebClient
- Return metric units: celsius, kmh, mm
- Use local MockWebServer for tests
- Preserve existing Actuator, Swagger UI, Docker, Compose behavior
