You are implementing Task 1 from the Current Weather Endpoint plan at `docs/superpowers/plans/2026-09-10-current-weather-endpoint.md`.

Read this first — it is your requirements, with the exact values to use verbatim:
Task brief: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-brief.md
Report file: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-report.md

## Your Task

Add Weather Domain Models and WMO Descriptions for the Current Weather Endpoint.

### Files to Create:
1. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/LocationRequest.java` - sealed input interface
2. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptions.java` - WMO code mapping
3. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ResolvedLocation.java` - internal location model
4. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/provider/ProviderCurrentWeather.java` - provider weather model
5. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/LocationResponse.java` - public location response
6. `src/main/java/de/fraunhofer/ipa/openclawtestservice/weather/api/CurrentWeatherResponse.java` - public weather response
7. `src/test/java/de/fraunhofer/ipa/openclawtestservice/weather/application/WeatherCodeDescriptionsTest.java` - WMO test

### Your Work:
1. Read the task brief at `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-brief.md`
2. Implement all required files
3. Run tests with `./mvnw --batch-mode test -Dtest=WeatherCodeDescriptionsTest`
4. Write your report to `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-report.md`
5. Commit your work

DO NOT dispatch subagents. Report only status, commits, test summary, and concerns.

Write your report to: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-1-report.md

Report format:
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions