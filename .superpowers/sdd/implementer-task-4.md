You are implementing Task 4 from the Spring Boot Service Scaffold plan at `docs/superpowers/plans/2026-09-10-spring-boot-service-scaffold.md`.

Read this first — it is your requirements, with the exact values to use verbatim:
Task brief: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-brief.md
Report file: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-report.md

## Your Task

Package and Run the Service with Docker Compose.

### Files to Create:
1. `.dockerignore` - Docker build context exclusions
2. `Dockerfile` - Two-stage non-root image
3. `compose.yaml` - Docker Compose configuration

### Key Requirements:
- Run the container as non-root user `app:app` on port 8080
- Build Linux AMD64 images only
- Include health check using `/actuator/health`
- Use image reference `ghcr.io/ipa-big/openclaw-test-service:${IMAGE_TAG:-latest}`

### Your Work:
1. Read the task brief at `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-brief.md` - this is your exact requirement source
2. Implement all required files
3. Verify with Docker Compose
4. Write your report to `/home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-report.md` with:
   - Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
   - Commits: git commit hashes
   - Tests: test count and pass rate
   - Concerns: any issues
5. Commit your work

DO NOT dispatch subagents. Report only status, commits, test summary, and concerns.

Write your report to: /home/openclaw/openclaw-test-service/.superpowers/sdd/task-4-report.md

Report format:
1. Status: DONE, DONE_WITH_CONCERNS, NEEDS_CONTEXT, or BLOCKED
2. Commits: list git commit hashes
3. Tests: test count and pass rate
4. Concerns: any issues or questions