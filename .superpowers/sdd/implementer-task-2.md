You are implementing Task 2 from the Current Weather Endpoint plan at `docs/superpowers/plans/2026-09-10-current-weather-endpoint.md`.

Read this first — it is your requirements, with the exact values to use verbatim:
Task brief: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-brief.md
Report file: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-report.md

## Your Task

Configure WebClient and Implement Geocoding for the Current Weather Endpoint.

### Files to Create/Modify:
1. `pom.xml` - Add WebClient, WebFlux test support, MockWebServer
2. `src/main/resources/application.yml` - Open-Meteo URLs and timeouts
3. `LocationNotFoundException.java` - Exception type
4. `WeatherProviderException.java` - Exception type
5. `WeatherProviderTimeoutException.java` - Exception type
6. `GeocodingClient.java` - Provider-independent interface
7. `OpenMeteoProperties.java` - Configuration properties
8. `OpenMeteoClientConfiguration.java` - WebClient configuration
9. `OpenMeteoGeocodingClient.java` - Implementation
10. `OpenMeteoGeocodingClientTest.java` - Tests

### Your Work:
1. Read the task brief at `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-brief.md`
2. Implement all required files
3. Run tests with `./mvnw --batch-mode test -Dtest=OpenMeteoGeocodingClientTest`
4. Write your report to `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-report.md`
5. Commit your work

DO NOT dispatch subagents. Report only status, commits, test summary, and concerns.

Write your report to: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-2-report.md

Report format:
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions