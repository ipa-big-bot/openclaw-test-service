# Task 4 Brief: Add Reactive Weather Orchestration

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherService.java`
- `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/CurrentWeatherServiceTest.java`

## Interfaces
- Consumes: `GeocodingClient`, `WeatherClient`, `LocationNotFoundException`, `WeatherProviderException`
- Produces: `CurrentWeatherService.getWeather(LocationRequest): Mono<CurrentWeatherResponse>`

## Steps
1. Write failing service orchestration tests
2. Run tests to verify failure
3. Implement the CurrentWeatherService
4. Run service tests
5. Commit reactive weather orchestration

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 4 of the Current Weather Endpoint plan. The goal is to compose geocoding and weather clients into a reactive orchestration.

## Global Constraints
- Use Java 21, Spring Boot 4.1.1, Spring WebFlux WebClient
- Do NOT use `.block()`, `.blockFirst()`, `.blockLast()`, or `.subscribe()` in production code
- Preserve existing Actuator, Swagger UI, Docker, Compose behavior
