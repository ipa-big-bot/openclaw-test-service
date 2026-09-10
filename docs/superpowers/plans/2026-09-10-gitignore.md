# .gitignore Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

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

### Task 1: Create .gitignore with Maven section

**Files:**
- Create: `.gitignore` (root)

**Interfaces:**
- Consumes: None
- Produces: `.gitignore` file with Maven patterns

- [ ] **Step 1: Create .gitignore with Maven section**

Create `.gitignore` with:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Maven patterns to .gitignore"
```

---

### Task 2: Add Gradle section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 1
- Produces: `.gitignore` with Gradle patterns

- [ ] **Step 1: Add Gradle section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Gradle patterns to .gitignore"
```

---

### Task 3: Add IntelliJ IDEA section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 2
- Produces: `.gitignore` with IntelliJ patterns

- [ ] **Step 1: Add IntelliJ section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add IntelliJ IDEA patterns to .gitignore"
```

---

### Task 4: Add VS Code section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 3
- Produces: `.gitignore` with VS Code patterns

- [ ] **Step 1: Add VS Code section to .gitignore**

Append to `.gitignore`:

```gitignore
# VS Code
.vscode/
*.code-workspace
```

- [ ] **Step 2: Verify VS Code patterns**

Run: `git check-ignore -v .vscode/` → should match `.vscode/`

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add VS Code patterns to .gitignore"
```

---

### Task 5: Add Eclipse section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 4
- Produces: `.gitignore` with Eclipse patterns

- [ ] **Step 1: Add Eclipse section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Eclipse patterns to .gitignore"
```

---

### Task 6: Add NetBeans section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 5
- Produces: `.gitignore` with NetBeans patterns

- [ ] **Step 1: Add NetBeans section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add NetBeans patterns to .gitignore"
```

---

### Task 7: Add Spring Boot section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 6
- Produces: `.gitignore` with Spring Boot patterns

- [ ] **Step 1: Add Spring Boot section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Spring Boot patterns to .gitignore"
```

---

### Task 8: Add Docker section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 7
- Produces: `.gitignore` with Docker patterns and `.env.example` negation

- [ ] **Step 1: Add Docker section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Docker patterns to .gitignore"
```

---

### Task 9: Add OS files section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 8
- Produces: `.gitignore` with OS file patterns

- [ ] **Step 1: Add OS section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add OS file patterns to .gitignore"
```

---

### Task 10: Add Logs section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 9
- Produces: `.gitignore` with log patterns

- [ ] **Step 1: Add Logs section to .gitignore**

Append to `.gitignore`:

```gitignore
# Logs
*.log
logs/
```

- [ ] **Step 2: Verify Logs patterns**

Run: `git check-ignore -v app.log` → should match `*.log`
Run: `git check-ignore -v logs/` → should match `logs/`

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add log patterns to .gitignore"
```

---

### Task 11: Add Testing section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 10
- Produces: `.gitignore` with testing patterns

- [ ] **Step 1: Add Testing section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add testing patterns to .gitignore"
```

---

### Task 12: Add Superpowers section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 11
- Produces: `.gitignore` with Superpowers patterns

- [ ] **Step 1: Add Superpowers section to .gitignore**

Append to `.gitignore`:

```gitignore
# Superpowers (generated files)
docs/superpowers/specs/
docs/superpowers/plans/
.copilot/
```

- [ ] **Step 2: Verify Superpowers patterns**

Run: `git check-ignore -v docs/superpowers/specs/` → should match `docs/superpowers/specs/`
Run: `git check-ignore -v docs/superpowers/plans/` → should match `docs/superpowers/plans/`

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add Superpowers patterns to .gitignore"
```

---

### Task 13: Add Additional Tools section

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 12
- Produces: `.gitignore` with additional tool patterns

- [ ] **Step 1: Add Additional Tools section to .gitignore**

Append to `.gitignore`:

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

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: add additional tool patterns to .gitignore"
```

---

### Task 14: Final verification and commit

**Files:**
- Modify: `.gitignore`

**Interfaces:**
- Consumes: `.gitignore` from Task 13
- Produces: Final `.gitignore` with all patterns

- [ ] **Step 1: Run full verification**

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

- [ ] **Step 2: Final commit**

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

- [ ] All patterns from spec are present in `.gitignore`
- [ ] Explanatory comments present for each section
- [ ] `.env.example` is NOT ignored (negation pattern works)
- [ ] All `git check-ignore` tests pass
- [ ] `.gitignore` commits and pushes successfully
- [ ] No false positives (tracked files incorrectly ignored)