# judo-meta-keycloak - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-keycloak
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.8.7+ with Eclipse Tycho 4.0.13

1. Defines an **Ecore metamodel** for Keycloak realm configurations (realms, clients, users, credentials, attribute bindings)
2. Generates **EMF Java code** (interfaces, implementations, builders) from the metamodel via MWE2 workflows
3. Provides **runtime utilities** for model querying (`KeycloakUtils`), JSON serialization (`KeycloakObjectMapper`, `KeycloakConfigurationExporter`), and Epsilon EVL validation (`KeycloakEpsilonValidator`)
4. Packages the model as both an **Eclipse plugin** (P2 update site) and a **standalone OSGi bundle** (with automatic model discovery via `KeycloakModelBundleTracker`)
5. Part of the **Judo framework** ecosystem for model-driven application development

## Directory Structure

```
judo-meta-keycloak/
├── model/                    # Core Eclipse plugin — metamodel + runtime
│   ├── model/                # keycloak.ecore, keycloak.genmodel
│   ├── src/main/java/        # Hand-written runtime classes
│   ├── src/main/epsilon/     # EVL validation scripts
│   ├── src/workflow/          # MWE2 code generation workflow
│   └── src-gen/              # Generated EMF code (do NOT edit)
├── model-test/               # JUnit 5 unit tests
├── osgi/                     # OSGi bundle repackaging
├── osgi-itest/               # Pax Exam integration tests (Karaf)
├── feature/                  # Eclipse feature definition
├── site/                     # Eclipse P2 update site
├── .github/workflows/        # CI/CD pipelines
└── openspec/                 # OpenSpec configuration and specs
```

## Core Modules

### Model Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | Eclipse plugin (Tycho) | Ecore metamodel definition, EMF-generated Java code (interfaces, impls, factory, package), and hand-written runtime classes for validation, JSON export, and Jackson deserialization |

### Distribution Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | OSGi bundle (Felix) | Standalone bundle for Karaf/non-Eclipse environments. Embeds Epsilon validation rules and provides `KeycloakModelBundleTracker` for automatic model discovery from bundle manifest headers |
| `feature/` | Eclipse feature | Installable feature for Eclipse IDE |
| `site/` | Eclipse repository | P2 update site for "Install New Software" |

### Test Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model-test/` | JAR (JUnit 5) | Unit tests for KeycloakUtils, KeycloakConfigurationExporter, KeycloakEpsilonValidator, and model execution context |
| `osgi-itest/` | JAR (Pax Exam) | Integration tests verifying model loading, service registration, and validation in an Apache Karaf container |

## Technology Stack

### Core Technologies
- **Eclipse EMF** 2.38.0 — Ecore metamodel framework (model definition, code generation)
- **Eclipse Tycho** 4.0.13 — Maven plugin for building Eclipse plugins, features, and P2 sites
- **Epsilon Runtime** 2.8.0 — EVL (Epsilon Validation Language) for model constraint checking
- **Jackson** 2.17.2 — JSON serialization/deserialization with custom EList support
- **OSGi Core** 7.0.0 — Service component model for bundle lifecycle
- **Apache Karaf** 4.4.7 — OSGi container for runtime deployment
- **Lombok** 1.18.34 — Annotation processing (non-Eclipse modules only)

### Build & Quality
- **Maven** 3.8.7+ with `./mvnw` wrapper
- **JUnit 5** (Jupiter) 5.9.1 — Unit testing
- **Pax Exam** 4.13.5 — OSGi integration testing
- **JaCoCo** 0.8.12 — Code coverage
- **SonarQube** — Static analysis (Maven plugin 3.9.1)
- **Flatten Maven Plugin** 1.3.0 — CI-friendly `${revision}` version resolution

## Build Commands

```bash
# Full build (code generation + compile + test)
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Run a single test class
./mvnw test -pl model-test -Dtest=KeycloakUtilsTest

# Full verification with OSGi integration tests
./mvnw clean verify

# Update target definition P2 URLs after dependency version changes
./mvnw clean install -P update-target-versions -f targetdefinition/pom.xml

# Update site category P2 URLs
./mvnw clean install -P update-category-versions -f site/pom.xml
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Active by default. Includes all submodules. Disable with `-DskipModules=true`. |
| `sign-artifacts` | GPG-signs artifacts for release. Requires GPG key in settings.xml. |
| `release-dummy` | Test deployment to `/tmp`. |
| `release-judong` | Deploy to Judong Nexus repository. |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH. |
| `update-target-versions` | Update Eclipse target definition P2 repository URLs. |
| `update-category-versions` | Update site category P2 repository URLs. |
| `generate-github-asciidoc-diagrams` | Generate PlantUML diagrams from AsciiDoc. |
| `update-source-code-license` | Update EPL-2.0 license headers on source files. |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM with dependency management, plugin management, and all profiles |
| `model/model/keycloak.ecore` | Ecore metamodel defining Realm, Client, User, UserCredential, AttributeBinding |
| `model/model/keycloak.genmodel` | EMF GenModel controlling Java code generation parameters |
| `model/src/workflow/generateModel.mwe2` | MWE2 workflow orchestrating EMF code generation (EcoreGenerator, HelperGenerator, BuilderGenerator, RuntimeModelGenerator) |
| `model/src/main/epsilon/validations/keycloak.evl` | Epsilon validation constraints for model integrity |
| `model/META-INF/MANIFEST.MF` | Eclipse plugin manifest with exported packages and dependencies |
| `osgi/bnd.bnd` or `pom.xml` bundle config | OSGi bundle manifest instructions (Felix Maven Bundle Plugin) |
| `logback-test.xml` | Logback configuration for test execution |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.8.7+ (or use `./mvnw`)
- BlackBelt Nexus credentials configured in Maven `settings.xml`

**For Eclipse IDE development:**
- m2e plugin
- Epsilon plugin
- XText, MWE2 plugins
- Eclipse Modeling Tools

**Code generation in Eclipse:**
Right-click `model/src/workflow/generateModel.mwe2` → Run As → MWE2 Workflow

## Git Workflow

- **Main Branch:** `develop`
- **Release Branch:** `master` (latest stable release)
- **Versioning:** `1.0.1-SNAPSHOT` (Maven) / `1.0.1.qualifier` (Eclipse)
- **Feature branches:** `feature/JNG-NUMBER_summary` from `develop`
- **Bugfix branches:** `bugfix/JNG-NUMBER_summary` from release branches
- **Hotfix branches:** `hotfix/JNG-NUMBER_summary` from `master`
- **Rule:** Every commit must reference a Jira ticket (`JNG-xxx`)
- CI builds append branch metadata to versions (e.g., `1.0.1.develop_40`)

## Important Notes

1. **Never edit `src-gen/` files** — they are regenerated by the MWE2 workflow on every build. All hand-written code belongs in `src/main/java/`.
2. **Tycho surefire `argLine` must stay on one line** — the `<argLine>` element in the parent pom.xml tycho-surefire-plugin config will break if auto-formatted across multiple lines.
3. **Lombok cannot be used in Eclipse plugin modules** — Tycho does not support Lombok annotation processing. Lombok is only used in standard JAR modules (tests).
4. **Version duality** — Maven uses `-SNAPSHOT` while Eclipse uses `.qualifier`. The Flatten Maven Plugin and Tycho Versions Plugin handle conversion automatically; do not manually sync these.
5. **P2 repository URLs contain hardcoded versions** — Tycho loads repository definitions before Maven property resolution. Use `update-target-versions` and `update-category-versions` profiles after changing dependency versions.
6. **Nexus credentials required** — Private BlackBelt artifacts require Maven settings.xml with `blackbelt-nexus-mirror` server credentials.
7. **Jackson EList deserialization** — `KeycloakObjectMapper` uses MixIn classes that extend `*Impl` classes with collection setters, enabling Jackson to deserialize JSON arrays into EMF `EList` collections.
8. **OSGi model discovery** — Bundles with a `Keycloak-Models` manifest header are automatically discovered by `KeycloakModelBundleTracker` and registered as OSGi services.

## Related Documentation

- [README.md](README.md) — Project overview with architecture diagrams
- [.github/CIFLOW.md](.github/CIFLOW.md) — Detailed CI/CD workflow and branching strategy documentation
