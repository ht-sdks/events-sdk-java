# Agent Instructions

This file provides guidance for updating dependencies in this Java Maven multi-module project.

## Project Overview

- **Language**: Java (source level 8)
- **Build Tool**: Maven (multi-module reactor)
- **Testing**: JUnit 4, Mockito, AssertJ, Burst
- **Linting/Formatting**: Spotless (Google Java Format)
- **API Compatibility**: animal-sniffer
- **CI**: GitHub Actions on JDK 8, 11, and 17

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

## Common Issues After Dependency Updates

### CI Failures

1. **Spotless failures**: Run `mvn spotless:apply` to auto-format, then verify with `mvn spotless:check`.
2. **animal-sniffer failures**: A dependency upgrade introduced an API not available in the target Java version (8). You may need to find an older version of the dependency or adjust usage.
3. **Test failures**: Check changelogs of updated dependencies for breaking changes. Common issues include Mockito API changes between major versions, Gson serialization behavior changes, and OkHttp API changes.
4. **Compilation errors**: AutoValue or Retrofit annotation processors may have changed. Try `mvn clean install` to clear stale generated sources in `target/generated-sources/`.

### Spring Boot Version Constraints

The `analytics-spring-boot-starter` module uses Spring Boot 2.x. Spring Boot 3.x requires Java 17+ and Jakarta EE (namespace change from `javax.*` to `jakarta.*`). Do not upgrade to Spring Boot 3.x without also bumping the project's Java baseline.

### Spotless / Google Java Format Version Coupling

The Spotless plugin (`2.27.2`) uses Google Java Format `1.5`, which is compatible with Java 8. Newer versions of Google Java Format require Java 11+. Do not upgrade Google Java Format without also updating the Java source level.

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
