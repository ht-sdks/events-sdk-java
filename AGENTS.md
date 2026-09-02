# Agent Instructions

This file provides guidance for updating dependencies in this Java Maven multi-module project.

## Project Overview

- **Language**: Java (source level 8)
- **Build Tool**: Maven (multi-module reactor)
- **Testing**: JUnit 4, Mockito, AssertJ, Burst
- **Linting/Formatting**: Spotless (Google Java Format)
- **API Compatibility**: animal-sniffer
- **CI**: GitHub Actions on JDK 8, 11, 17, 21, and 25

### Dependency Version Management

All dependency versions are centralized as `<properties>` in the root `pom.xml` and referenced via `${property.name}` in the `<dependencyManagement>` section. When updating a dependency version, you typically only need to change the property value in the root `pom.xml`.

Refer to the `<properties>` block in the root `pom.xml` for the current list of version properties and their values.

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

# Run tests and the CLI fat-JAR smoke test (every JDK).
# Formatting and API compatibility run on JDK 17.
mvn -B verify
mvn -B spotless:check
mvn -B animal-sniffer:check
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
mvn versions:update-properties -Dincludes=com.google.code.gson:gson:jar::

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

The CI runs on JDK 8, 11, 17, 21, and 25, but not every check is matrixed:

1. `mvn -B verify` on every JDK (unit tests plus the analytics-cli fat-JAR `--help` smoke test)
2. `mvn -B spotless:check` on JDK 17 only
3. `mvn -B animal-sniffer:check` on JDK 17 only (Java 8 API signature)

Local formatting and animal-sniffer can require JDK 17. If you have multiple JDK versions available:

```bash
# Test and smoke-test the analytics-cli assembly JAR
JAVA_HOME=/path/to/jdk8 mvn -B verify
JAVA_HOME=/path/to/jdk11 mvn -B verify

# Same on JDK 17, plus Spotless and animal-sniffer in the same reactor
JAVA_HOME=/path/to/jdk17 mvn -B verify spotless:check animal-sniffer:check
JAVA_HOME=/path/to/jdk21 mvn -B verify
JAVA_HOME=/path/to/jdk25 mvn -B verify
```

---

## Common Issues After Dependency Updates

### CI Failures

1. **Spotless failures**: Run `mvn spotless:apply` to auto-format, then verify with `mvn spotless:check`.
2. **animal-sniffer failures**: A dependency upgrade introduced an API not available in the target Java version (8). You may need to find an older version of the dependency or adjust usage.
3. **Test failures**: Check changelogs of updated dependencies for breaking changes. Common issues include Mockito API changes between major versions, Gson serialization behavior changes, and OkHttp API changes.
4. **Compilation errors**: AutoValue or Retrofit annotation processors may have changed. Try `mvn clean install` to clear stale generated sources in `target/generated-sources/`. JDK 22+ does not run annotation processors found only on the classpath; AutoValue is registered via `annotationProcessorPaths` on the compiler plugin.

### Spring Boot Version Constraints

The `analytics-spring-boot-starter` module compiles against Spring Boot 3.5.x (last OSS 3.x line) so published artifacts resolve Spring Framework 6.2.19+, which includes the CVE-2025-41249 fix. Spring Framework 6 and Spring Boot 3 require JDK 17+ to compile against; the starter is therefore only in the Maven reactor when the JDK is 17 or newer. The rest of the SDK stays on Java 8. Spring Boot 4.x splits `spring-boot-autoconfigure` into per-technology modules — do not upgrade to 4.x without reviewing that packaging change.

### Spotless / Google Java Format Version Coupling

CI runs Spotless on JDK 17. The plugin (`2.46.1`) requires JRE 11+. Google Java Format `1.17.0` is the minimum Spotless 2.46.1 accepts on JDK 21 and also runs on the JDK 17 CI job. Newer Spotless/GJF versions may be used without raising the library's Java 8 baseline. Newer GJF typically needs JDK 11+ to execute and may reformat existing files.

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
