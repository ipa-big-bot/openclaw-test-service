# Task 2 Brief: Configure WebClient and Implement Geocoding

Read this first — it is your requirements, with the exact values to use verbatim.

## Files to Create
- Modify: `pom.xml` (add WebClient, WebFlux test support, MockWebServer)
- Modify: `src/main/resources/application.yml` (Open-Meteo URLs and timeouts)
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationNotFoundException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherProviderTimeoutException.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/GeocodingClient.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoProperties.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoClientConfiguration.java`
- Create: `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClient.java`
- Test: `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/openmeteo/OpenMeteoGeocodingClientTest.java`

## Interfaces
- Produces: `GeocodingClient.resolve(String): Mono<ResolvedLocation>` plus typed exceptions.

## Steps
1. Add WebClient and test dependencies
2. Add Open-Meteo production configuration
3. Write failing geocoding client tests
4. Run tests to verify failure
5. Add typed provider exceptions and geocoding interface
6. Add typed properties and timeout-enabled WebClients
7. Implement the Open-Meteo geocoding client
8. Run geocoding client tests
9. Commit WebClient geocoding

## Report file
Write your report to: `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-report.md`

**Report format:**
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions

## Context
This is Task 2 of the Current Weather Endpoint plan. The goal is to add WebClient configuration and Open-Meteo geocoding client.

## Global Constraints
- Use Java 21, Spring Boot 4.1.1, Spring WebFlux WebClient
- Automated tests use local mock HTTP servers (MockWebServer 4.12.0)
- Do not call live Open-Meteo endpoints
- Preserve existing Actuator, Swagger UI, Docker, Compose behavior
