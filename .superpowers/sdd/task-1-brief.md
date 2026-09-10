# Task 1 Brief: Add Weather Domain Models and WMO Descriptions

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationRequest.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptions.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ResolvedLocation.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ProviderCurrentWeather.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/LocationResponse.java`
- `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherResponse.java`
- `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java`

## Interfaces
- Produces: `LocationRequest.City`, `LocationRequest.Coordinates`, `ResolvedLocation`, `ProviderCurrentWeather`, `LocationResponse`, `CurrentWeatherResponse`, and `WeatherCodeDescriptions.descriptionFor(int)`.

## Steps
1. Write failing weather-code mapping test
2. Run test to verify failure
3. Add location request and internal provider models
4. Add public response records
5. Implement weather-code mapping
6. Run weather-code test
7. Commit the weather models

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 1 of the Current Weather Endpoint plan. The goal is to add weather domain models and WMO description mapping.

## Global Constraints
- Use Java 21.
- Use Maven coordinates `de.fraunhofer.ipa:openclaw-test-service`.
- Use Java base package `de.fraunhofer.ipa.openclawtestservice`.
- Use Spring Boot 4.1.1.
- Keep existing Actuator, Swagger UI, Docker, Compose, and GitHub Actions behavior.
