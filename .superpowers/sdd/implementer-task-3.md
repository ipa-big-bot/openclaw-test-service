You are implementing Task 3 from the Spring Boot Service Scaffold plan at `docs/superpowers/plans/2026-09-10-spring-boot-service-scaffold.md`.

Read this first — it is your requirements, with the exact values to use verbatim:
Task brief: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-brief.md
Report file: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-report.md

## Your Task

Add OpenAPI Documentation to the Spring Boot Service.

### Files to Create/Modify:
1. Modify `pom.xml` (add Springdoc dependency)
2. Create `src/main/java/de/fraunhofer/ipa/openclawtestservice/config/OpenApiConfiguration.java`
3. Create `src/test/java/de/fraunhofer/ipa/openclawtestservice/OpenApiEndpointIT.java`

### Key Requirements:
- Use Spring Boot 4.1.1
- Use Springdoc OpenAPI 3.1.1
- Expose OpenAPI JSON at `/v3/api-docs`
- Expose Swagger UI at `/swagger-ui/index.html`

### Your Work:
1. Read the task brief at `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-brief.md` - this is your exact requirement source
2. Implement all required files
3. Run tests with `./mvnw --batch-mode verify`
4. Write your report to `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-report.md` with:
   - Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
   - Commits: git commit hashes
   - Tests: test count and pass rate
   - Concerns: any issues
5. Commit your work

DO NOT dispatch subagents. Report only status, commits, test summary, and concerns.

Write your report to: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-3-report.md

Report format:
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions