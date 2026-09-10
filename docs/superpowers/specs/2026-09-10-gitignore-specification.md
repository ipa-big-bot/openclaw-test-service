# .gitignore Specification

> **For agentic workers:** After approval, invoke superpowers:writing-plans to create implementation tasks.

**Goal:** Create a comprehensive `.gitignore` file for a Spring Boot + Maven + Gradle + Docker project with IntelliJ and VS Code IDE support, tracking `.env.example` while ignoring `.env` files, and excluding superpowers-generated files.

**Architecture:** Single `.gitignore` file at project root with categorized sections, explanatory comments, and negation patterns for tracked templates.

**Tech Stack:** Git, Spring Boot, Maven, Gradle, Docker, IntelliJ IDEA, VS Code

## Global Constraints

- **Format:** Explanatory comments for each section, one pattern per line
- **IDEs:** IntelliJ IDEA, VS Code (Eclipse/NetBeans optional)
- **Build tools:** Maven, Gradle
- **Environment:** Track `.env.example`, ignore `.env` and `.env.*`
- **Testing:** JUnit test output
- **Superpowers:** Exclude files created by superpowers skill (specs, plans, session artifacts)
- **Spring Boot:** Ignore `application*.properties`, `application*.yml`, `.spring-boot/`
- **Docker:** Ignore `.env`, `.env.*`, `*.env`
- **OS files:** `.DS_Store`, `Thumbs.db`, `desktop.ini`
- **Logs:** `*.log`, `logs/`
- **Build output:** `target/`, `build/`, `dist/`, `out/`

---

## Section 1: Java/Maven

**Files:**
- Create: `.gitignore` (root)

**Patterns:**
- Maven build output: `target/`, `pom.xml.tag`, `pom.xml.releaseBackup`, `pom.xml.versionsBackup`, `pom.xml.next`, `release.properties`, `dependency-reduced-pom.xml`, `buildNumber.properties`
- Maven cache: `.mvn/`
- Maven wrapper: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.jar`, `.mvn/wrapper/maven-wrapper.properties`

- [ ] **Step 1: Add Maven section to .gitignore**

Add to `.gitignore`:

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/
```

- [ ] **Step 2: Verify Maven patterns**

Run: `git check-ignore -v target/` → should match `target/`
Run: `git check-ignore -v .mvn/` → should match `.mvn/`

---

## Section 2: Gradle

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Gradle build: `build/`, `.gradle/`
- Gradle wrapper: `gradle-app.setting`, `!gradle/wrapper/gradle-wrapper.jar`, `!**/src/main/**/build/`, `!**/src/test/**/build/`
- Gradle wrapper properties: `gradle-wrapper.properties`

- [ ] **Step 1: Add Gradle section to .gitignore**

Add to `.gitignore`:

```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/
gradle-app.setting
!gradle-wrapper.properties
```

- [ ] **Step 2: Verify Gradle patterns**

Run: `git check-ignore -v build/` → should match `build/`
Run: `git check-ignore -v .gradle/` → should match `.gradle/`

---

## Section 3: IntelliJ IDEA

**Files:**
- Modify: `.gitignore`

**Patterns:**
- IntelliJ config: `.idea/`
- IntelliJ project files: `*.iml`, `*.iws`, `*.ipr`
- IntelliJ generated: `.apt_generated/`

- [ ] **Step 1: Add IntelliJ section to .gitignore**

Add to `.gitignore`:

```gitignore
# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr
.apt_generated/
```

- [ ] **Step 2: Verify IntelliJ patterns**

Run: `git check-ignore -v .idea/` → should match `.idea/`
Run: `git check-ignore -v MyProject.iml` → should match `*.iml`

---

## Section 4: VS Code

**Files:**
- Modify: `.gitignore`

**Patterns:**
- VS Code config: `.vscode/`
- VS Code workspace: `*.code-workspace`

- [ ] **Step 1: Add VS Code section to .gitignore**

Add to `.gitignore`:

```gitignore
# VS Code
.vscode/
*.code-workspace
```

- [ ] **Step 2: Verify VS Code patterns**

Run: `git check-ignore -v .vscode/` → should match `.vscode/`

---

## Section 5: Eclipse

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Eclipse config: `.settings/`, `.project`, `.classpath`, `.factorypath`
- Eclipse Spring: `.springBeans`
- Eclipse IDE: `*.eclipse`

- [ ] **Step 1: Add Eclipse section to .gitignore**

Add to `.gitignore`:

```gitignore
# Eclipse
.settings/
.project
.classpath
.factorypath
.springBeans
```

- [ ] **Step 2: Verify Eclipse patterns**

Run: `git check-ignore -v .settings/` → should match `.settings/`

---

## Section 6: NetBeans

**Files:**
- Modify: `.gitignore`

**Patterns:**
- NetBeans private: `/nbproject/private/`
- NetBeans build: `/nbbuild/`, `/dist/`, `/nbdist/`
- NetBeans gradle: `/.nb-gradle/`

- [ ] **Step 1: Add NetBeans section to .gitignore**

Add to `.gitignore`:

```gitignore
# NetBeans
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
```

- [ ] **Step 2: Verify NetBeans patterns**

Run: `git check-ignore -v nbproject/private/` → should match `/nbproject/private/`

---

## Section 7: Spring Boot

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Application configs: `application*.yml`, `application*.yaml`, `application*.properties`
- Spring Boot cache: `.spring-boot/`
- Spring Boot DevTools: `spring-boot-remote.properties`

- [ ] **Step 1: Add Spring Boot section to .gitignore**

Add to `.gitignore`:

```gitignore
# Spring Boot
application*.yml
application*.yaml
application*.properties
spring-configuration-metadata.json
.spring-boot/
```

- [ ] **Step 2: Verify Spring Boot patterns**

Run: `git check-ignore -v application.properties` → should match `application*.properties`
Run: `git check-ignore -v .spring-boot/` → should match `.spring-boot/`

---

## Section 8: Docker

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Environment files: `.env`, `.env.*`, `*.env` (but not `.env.example`)
- Docker cache: `.docker/`

- [ ] **Step 1: Add Docker section to .gitignore**

Add to `.gitignore`:

```gitignore
# Docker
.env
.env.*
*.env
!.env.example
.docker/
```

- [ ] **Step 2: Verify Docker patterns**

Run: `git check-ignore -v .env` → should match `.env`
Run: `git check-ignore -v .env.example` → should NOT match (negation pattern)

---

## Section 9: OS Files

**Files:**
- Modify: `.gitignore`

**Patterns:**
- macOS: `.DS_Store`, `.DS_Store?`, `._*`
- Windows: `Thumbs.db`, `ehthumbs.db`, `Desktop.ini`
- Linux: `*~`, `.directory`

- [ ] **Step 1: Add OS section to .gitignore**

Add to `.gitignore`:

```gitignore
# macOS
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs_vista.db

# Windows
Thumbs.db
ehthumbs.db
Desktop.ini
$RECYCLE.BIN/

# Linux
*~
.directory
```

- [ ] **Step 2: Verify OS patterns**

Run: `git check-ignore -v .DS_Store` → should match `.DS_Store`
Run: `git check-ignore -v Thumbs.db` → should match `Thumbs.db`

---

## Section 10: Logs

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Log files: `*.log`, `logs/`
- Log directories: `logs/`

- [ ] **Step 1: Add Logs section to .gitignore**

Add to `.gitignore`:

```gitignore
# Logs
*.log
logs/
```

- [ ] **Step 2: Verify Logs patterns**

Run: `git check-ignore -v app.log` → should match `*.log`
Run: `git check-ignore -v logs/` → should match `logs/`

---

## Section 11: Testing

**Files:**
- Modify: `.gitignore`

**Patterns:**
- JUnit: `test-output/`, `test-results/`, `junit-platform.properties`
- Coverage: `coverage/`, `.jacoco/`, `jacoco.exec`, `jacoco.csv`
- Test reports: `surefire-reports/`, `failsafe-reports/`

- [ ] **Step 1: Add Testing section to .gitignore**

Add to `.gitignore`:

```gitignore
# Testing
test-output/
test-results/
junit-platform.properties
coverage/
.jacoco/
jacoco.exec
jacoco.csv
surefire-reports/
failsafe-reports/
```

- [ ] **Step 2: Verify Testing patterns**

Run: `git check-ignore -v test-results/` → should match `test-results/`
Run: `git check-ignore -v coverage/` → should match `coverage/`

---

## Section 12: Superpowers Files

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Superpowers specs: `docs/superpowers/specs/`
- Superpowers plans: `docs/superpowers/plans/`
- Superpowers session: `.copilot/`

- [ ] **Step 1: Add Superpowers section to .gitignore**

Add to `.gitignore`:

```gitignore
# Superpowers (generated files)
docs/superpowers/specs/
docs/superpowers/plans/
.copilot/
```

- [ ] **Step 2: Verify Superpowers patterns**

Run: `git check-ignore -v docs/superpowers/specs/` → should match `docs/superpowers/specs/`
Run: `git check-ignore -v docs/superpowers/plans/` → should match `docs/superpowers/plans/`

---

## Section 13: Additional Tools

**Files:**
- Modify: `.gitignore`

**Patterns:**
- Lombok: `lombok.config`, `lombok.delombok/`
- JRebel: `rebel.xml`, `rebel-remote.xml`
- YourKit: `.yjp/`
- VisualVM: `visualvm/`
- Ant: `build/`, `dist/`
- Bash: `*.sh`, `*.bash`
- PowerShell: `*.ps1`, `*.psm1`, `*.psd1`

- [ ] **Step 1: Add Additional Tools section to .gitignore**

Add to `.gitignore`:

```gitignore
# Lombok
lombok.config
lombok.delombok/

# JRebel
rebel.xml
rebel-remote.xml

# YourKit
.yjp/

# VisualVM
visualvm/

# Ant
build/
dist/

# Bash
*.sh
*.bash

# PowerShell
*.ps1
*.psm1
*.psd1
```

- [ ] **Step 2: Verify Additional Tools patterns**

Run: `git check-ignore -v lombok.config` → should match `lombok.config`
Run: `git check-ignore -v rebel.xml` → should match `rebel.xml`

---

## Verification

- [ ] **Step 1: Run git check-ignore on all patterns**

```bash
# Test Maven
git check-ignore -v target/
git check-ignore -v .mvn/

# Test Gradle
git check-ignore -v build/
git check-ignore -v .gradle/

# Test IntelliJ
git check-ignore -v .idea/
git check-ignore -v MyProject.iml

# Test VS Code
git check-ignore -v .vscode/

# Test Spring Boot
git check-ignore -v application.properties
git check-ignore -v .spring-boot/

# Test Docker
git check-ignore -v .env
git check-ignore -v .env.example  # should NOT match

# Test OS
git check-ignore -v .DS_Store
git check-ignore -v Thumbs.db

# Test Logs
git check-ignore -v app.log
git check-ignore -v logs/

# Test Testing
git check-ignore -v test-results/
git check-ignore -v coverage/

# Test Superpowers
git check-ignore -v docs/superpowers/specs/
git check-ignore -v docs/superpowers/plans/
```

- [ ] **Step 2: Commit .gitignore**

```bash
git add .gitignore
git commit -m "chore: add comprehensive .gitignore for Spring Boot + Docker" -m "- Java/Maven: target/, .mvn/, IDE metadata
- Gradle: build/, .gradle/, wrapper
- IDEs: IntelliJ, VS Code, Eclipse, NetBeans
- OS: .DS_Store, Thumbs.db, desktop.ini
- Logs: *.log, logs/
- Testing: test output, coverage reports
- Spring Boot: application*.properties, .spring-boot/
- Docker: .env files, track .env.example
- Superpowers: specs, plans, session artifacts
- Additional: Lombok, JRebel, profiling tools" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

- [ ] **Step 3: Push to remote**

```bash
git push origin main
```

---

## Files Created/Modified

- **Create:** `.gitignore` (root)
- **Modify:** None (new file)

## Testing Strategy

1. **Pattern verification:** Use `git check-ignore -v <path>` to verify each pattern
2. **Negation verification:** Ensure `.env.example` is NOT ignored
3. **Commit verification:** Verify `.gitignore` commits without errors
4. **Push verification:** Ensure push succeeds without conflicts

## Success Criteria

- [ ] All patterns in Sections 1-13 are present in `.gitignore`
- [ ] Explanatory comments present for each section
- [ ] `.env.example` is NOT ignored (negation pattern works)
- [ ] All `git check-ignore` tests pass
- [ ] `.gitignore` commits and pushes successfully
- [ ] No false positives (tracked files incorrectly ignored)