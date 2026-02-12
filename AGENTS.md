# Agent Instructions

This file provides instructions for AI agents working on this Java Maven multi-module project.

## Project Overview

- **Language**: Java (source level 8)
- **Build Tool**: Maven (multi-module reactor)
- **Testing**: JUnit 4, Mockito, AssertJ, Burst
- **Linting/Formatting**: Spotless (Google Java Format)
- **API Compatibility**: animal-sniffer
- **Distribution**: JitPack

### Project Structure

```
analytics-core/                   # Core types, messages, HTTP service (published)
analytics/                        # Main Analytics client (published)
analytics-sample/                 # Sample usage application (not published)
analytics-cli/                    # CLI tool (not published)
analytics-spring-boot-starter/    # Spring Boot auto-configuration (published)
.github/workflows/                # CI for JDK 8, 11, and 17
.buildscript/                     # Bootstrap scripts
```

### Module Dependencies

```
analytics ──► analytics-core
analytics-sample ──► analytics
analytics-cli ──► analytics-core
analytics-spring-boot-starter ──► analytics
```

### Dependency Version Management

All dependency versions are centralized as `<properties>` in the root `pom.xml` and referenced via `${property.name}` in the `<dependencyManagement>` section. When updating a dependency version, you typically only need to change the property value in the root `pom.xml`.

Key version properties:

| Property | Artifact | Current |
|---|---|---|
| `retrofit.version` | Retrofit | 2.9.0 |
| `gson.version` | Gson | 2.9.1 |
| `okhttp.version` | OkHttp | 4.10.0 |
| `logging.version` | OkHttp Logging Interceptor | 4.10.0 |
| `guava.version` | Guava | 31.1-jre |
| `auto.version` | AutoValue | 1.10.1 |
| `kotlin.version` | Kotlin stdlib | 1.7.20 |
| `spring.boot.version` | Spring Boot | 2.7.5 |
| `mockito.version` | Mockito | 4.11.0 |
| `assertj.version` | AssertJ | 3.24.2 |
| `junit.version` | JUnit 4 | 4.13.2 |
| `spotless.version` | Spotless | 2.27.2 |

---

## Updating Dependencies

### 1. Pre-flight Checks

```bash
# Check Java version
java -version

# Check Maven version
mvn --version

# Ensure you're at the repository root (where the parent pom.xml lives)
pwd  # Should contain: pom.xml, analytics-core/, analytics/, etc.
```

### 2. Establish Test Baseline

```bash
# Install all modules (downloads dependencies and builds)
mvn install -DskipTests

# Run formatting check, API compatibility check, and all tests
mvn spotless:check animal-sniffer:check test verify

# Build all packages
mvn -B package
```

Record the number of passing tests before making any changes. This ensures you can verify nothing broke after upgrading. Look for the `Tests run:` summary lines in the Maven output for each module.

### 3. Check for Security Advisories

Maven does not have a built-in audit command like npm. Use the OWASP dependency-check plugin:

```bash
# Run OWASP dependency check (one-time, no need to add to pom.xml)
mvn org.owasp:dependency-check-maven:check
```

Alternatively, review dependencies manually on [MVN Repository](https://mvnrepository.com/) or use `mvn dependency:tree` to see the full transitive dependency tree.

### 4. Check Outdated Packages

```bash
# Display available updates for all dependencies
mvn versions:display-dependency-updates

# Display available updates for all plugins
mvn versions:display-plugin-updates

# Display available updates for properties that control versions
mvn versions:display-property-updates
```

This shows the current version and the latest available version for each dependency.

### 5. Upgrade Dependencies

#### Option A: Safe Updates (within current ranges)

Since this project pins exact versions via properties, "safe" updates mean bumping to the latest patch/minor version of the same major line.

Edit the `<properties>` block in the root `pom.xml`:

```xml
<properties>
  <!-- Example: bump Gson from 2.9.1 to 2.10.1 -->
  <gson.version>2.10.1</gson.version>
</properties>
```

#### Option B: Using the versions-maven-plugin

```bash
# Update all properties to their latest versions
mvn versions:update-properties

# Or update a specific property
mvn versions:update-properties -Dincludes=com.google.code.gson:gson

# Review what changed
git diff pom.xml
```

#### Option C: Major Version Updates

For major version bumps, edit the property values in the root `pom.xml` directly. Major bumps may require code changes (e.g., API changes in OkHttp 4.x vs 5.x).

After editing:

```bash
# Resolve and download new dependencies
mvn install -DskipTests
```

### 6. Rebuild and Test

```bash
# Run formatting/lint check
mvn spotless:check

# Run API compatibility check
mvn animal-sniffer:check

# Run all tests
mvn test

# Run full verification
mvn verify

# Build all packages
mvn -B package
```

Compare test results to your baseline from step 2. Fix any failures before proceeding.

### 7. Verify CI Would Pass

The CI runs on JDK 8, 11, and 17. The CI pipeline executes two steps:

1. `mvn spotless:check animal-sniffer:check test verify`
2. `mvn -B package`

If you have multiple JDK versions available locally:

```bash
# Test with JDK 8 (minimum supported)
JAVA_HOME=/path/to/jdk8 mvn spotless:check animal-sniffer:check test verify && mvn -B package

# Test with JDK 11
JAVA_HOME=/path/to/jdk11 mvn spotless:check animal-sniffer:check test verify && mvn -B package

# Test with JDK 17
JAVA_HOME=/path/to/jdk17 mvn spotless:check animal-sniffer:check test verify && mvn -B package
```

---

## Module-Specific Notes

### Core Module (`analytics-core`)

Foundation module with message types, HTTP service definitions, and serialization. Update this module first when doing cross-module upgrades since `analytics` depends on it.

Key dependencies: Retrofit, Gson, AutoValue, Guava (provided scope), FindBugs annotations.

### Analytics Module (`analytics`)

Main client library. Depends on `analytics-core` and adds OkHttp, Backo (retry backoff), and Retrofit converters.

Contains a **generated version file** via the `templating-maven-plugin` that injects `${project.version}` at build time.

### Spring Boot Starter (`analytics-spring-boot-starter`)

Auto-configuration for Spring Boot. Depends on the `analytics` module and Spring Boot Autoconfigure.

When updating Spring Boot, be aware of compatibility with the Java 8 source level. Spring Boot 3.x requires Java 17+, so only Spring Boot 2.x is compatible with this project's current Java 8 baseline.

---

## Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) with [Google Java Format](https://github.com/google/google-java-format) (version 1.5, GOOGLE style).

```bash
# Check formatting (fails if code is not formatted)
mvn spotless:check

# Auto-fix formatting
mvn spotless:apply
```

### Formatting Gotchas

- Google Java Format enforces a specific style that may differ from IDE defaults. Always run `mvn spotless:apply` before committing.
- The Spotless plugin version (`2.27.2`) uses Google Java Format `1.5` which is compatible with Java 8. Newer versions of Google Java Format require Java 11+. Do not upgrade Google Java Format without also updating the Java source level.

---

## CI/CD

- CI configs: `.github/workflows/java8.yml`, `.github/workflows/java11.yml`, `.github/workflows/java17.yml`
- All three workflows run the same steps on JDK 8, 11, and 17 respectively
- Steps:
  1. `mvn spotless:check animal-sniffer:check test verify`
  2. `mvn -B package` (only if step 1 succeeds)

### CI Failures After Dependency Updates

1. **Spotless failures**: Run `mvn spotless:apply` to auto-format, then verify with `mvn spotless:check`.
2. **animal-sniffer failures**: A dependency upgrade introduced an API not available in the target Java version (8). You may need to find an older version of the dependency or adjust usage.
3. **Test failures**: Check changelogs of updated dependencies for breaking changes. Common issues:
   - Mockito API changes between major versions
   - Gson serialization behavior changes
   - OkHttp API changes
4. **Compilation errors**: AutoValue or Retrofit annotation processors may have changed. Check generated source output in `target/generated-sources/`.

---

## Publishing / Releasing

Publishing is done via JitPack from git tags. See `RELEASING.md` for the full process.

### Versioning

- **PATCH** (0.0.1 -> 0.0.2): Bug fixes, safe dependency updates
- **MINOR** (0.1.0 -> 0.2.0): New backwards-compatible features
- **MAJOR** (0.0.x -> 1.0.0): Breaking API changes

### Files to Update for a Version Bump

All `pom.xml` files reference the parent version. Use the Maven release plugin or manually update:

1. Root `pom.xml` -> `<version>X.Y.Z-SNAPSHOT</version>`
2. Each module's `pom.xml` -> `<parent><version>X.Y.Z-SNAPSHOT</version></parent>`

Or use Maven to do it automatically:

```bash
mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
mvn versions:commit
```

---

## Common Issues

### Java Version Compatibility

This project targets Java 8. The `animal-sniffer` plugin verifies API compatibility. If a dependency update pulls in classes only available in newer Java versions, animal-sniffer will catch it.

```bash
# Check API compatibility
mvn animal-sniffer:check
```

### Spring Boot Version Constraints

The `analytics-spring-boot-starter` module uses Spring Boot 2.x. Spring Boot 3.x requires Java 17+ and Jakarta EE (namespace change from `javax.*` to `jakarta.*`). Do not upgrade to Spring Boot 3.x without also bumping the project's Java baseline.

### Transitive Dependency Conflicts

Use the dependency tree to diagnose version conflicts:

```bash
# Full dependency tree
mvn dependency:tree

# For a specific module
mvn dependency:tree -pl analytics

# Filter for a specific artifact
mvn dependency:tree -Dincludes=com.squareup.okhttp3
```

### AutoValue / Annotation Processor Issues

If builds fail with missing generated classes:

```bash
# Clean and rebuild from scratch
mvn clean install
```

AutoValue generates source files during compilation. Stale generated sources in the `target/` directory can cause issues after dependency changes.

---

## Quick Reference

| Task | Command |
|------|---------|
| Install dependencies | `mvn install -DskipTests` |
| Clean build | `mvn clean install` |
| Run tests | `mvn test` |
| Run full verification | `mvn verify` |
| Check formatting | `mvn spotless:check` |
| Fix formatting | `mvn spotless:apply` |
| Check API compat | `mvn animal-sniffer:check` |
| Build packages | `mvn -B package` |
| Full CI check | `mvn spotless:check animal-sniffer:check test verify && mvn -B package` |
| Check outdated deps | `mvn versions:display-dependency-updates` |
| Check outdated plugins | `mvn versions:display-plugin-updates` |
| View dependency tree | `mvn dependency:tree` |
| Update version | `mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT` |
