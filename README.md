# Judo Keycloak Meta

Keycloak metamodel for JUDO platform.

It acts as an Eclipse plugin with features and sites, can be used standalone and in standard OSGi (without Eclipse).

## Modules

- **targetdefinition** - Eclipse target definition defines the P2 repositories for all required MANIFEST features.
- **model** - Eclipse plugin containing the model and Ecore generated Java classes. Builder and Helpers added with MWE2 workflow.
- **model-test** - Unit tests for the metamodel and validation.
- **osgi** - OSGi bundle. Repackages model and adds extra information/services for transformation pipeline in other platforms.
- **feature** - Eclipse feature repository for Eclipse installation.
- **site** - Eclipse Update Site - all built versions compiled as an update site.

## Validation

The project supports two validation engines that run in parallel:

### EVL (Epsilon Validation Language)

Traditional validation using Epsilon scripts located in `model/src/main/epsilon/validations/`.

### Java (Zeta Framework)

Native Java validation using the [Zeta Validation Framework](https://github.com/BlackBeltTechnology/judo-zeta). Provides:

- Better IDE integration (debugging, refactoring, code completion)
- Compile-time type safety
- Improved performance (5-10x faster for large models)

Java validation classes are located in `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/`.

See [Validation Documentation](docs/validation/README.md) for details.

## Version Policy

Two worlds collide in this project. Maven and Eclipse have different views about versions:

- Maven: `1.0.0-SNAPSHOT`
- Eclipse: `1.0.0.qualifier`

Tycho versions plugin is used to replace qualifier and Maven versions for a technical version in every build.

## CI Build

All built artifacts are tagged in git: https://github.com/BlackBeltTechnology/judo-meta-keycloak/releases

All commits are built by Wercker: https://app.wercker.com/BlackbeltTechnology/judo-meta-keycloak/runs

## Installation

### Eclipse P2 Site

Install new software and add the URL of site listed on GitHub or the uncompressed ZIP folder.

### Maven Dependency

```xml
<dependency>
    <groupId>hu.blackbelt.judo.meta</groupId>
    <artifactId>hu.blackbelt.judo.meta.keycloak.model</artifactId>
    <version>${keycloak-version}</version>
</dependency>
```

## Compilation

### Prerequisites

The following plugins must be installed in Eclipse:
- m2e
- Epsilon
- Modeling tools

Or use the prebuilt Judo Eclipse.

### Maven Build

Minimum Maven version required: 3.5.4

```bash
mvn clean install
```

Or using Maven wrapper:

```bash
./mvnw clean install
```

### Maven Settings

Add BlackBelt Nexus repository to your settings.xml:

```xml
<servers>
    <server>
        <id>blackbelt-nexus-mirror</id>
        <username>${env.BLACKBELT_NEXUS_USERNAME}</username>
        <password>${env.BLACKBELT_NEXUS_PASSWORD}</password>
    </server>
</servers>

<mirrors>
    <mirror>
        <id>blackbelt-nexus-mirror</id>
        <name>blackbelt-nexus-mirror</name>
        <url>https://nexus.blackbelt.cloud/repository/maven</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
</mirrors>
```

### Maven Profiles

- **modules** - Activated by default. Contains all submodules.
- **sign-artifacts** - GPG sign all artifacts.
- **release-dummy** - Test deploy to /tmp directory.
- **release-blackbelt** - Release to BlackBelt Nexus.
- **release-central** - Release to Maven Central.

### Code Generation in Eclipse

Run as MWE2 Workflow:
```
hu.blackbelt.judo.meta.keycloak.model project src/workflow/generateModel.mwe2
```

Requires XTend, XText, MWE and MWE2 features.

## Running Tests

### Unit Tests

```bash
mvn test
```

### Performance Tests

```bash
mvn test -Dtest=KeycloakValidationPerformanceTest -Dperformance.test=true
```

Configure with system properties:
- `perf.realms` - Number of realms (default: 10)
- `perf.clients` - Clients per realm (default: 100)
- `perf.users` - Users per realm (default: 100)
- `perf.iterations` - Benchmark iterations (default: 3)

## Troubleshooting

### JUnit Tests in Eclipse

Add to classpath if JUnit not found:
```
<classpathentry kind="con" path="org.eclipse.jdt.junit.JUNIT_CONTAINER/5"/>
```

See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=534587

### Lombok Issues

Tycho does not support Lombok generation directly. No Lombok is used in Eclipse projects; all codes are generated.

See: https://github.com/rzwitserloot/lombok/issues/285

### Tycho Issues

Tycho 1.4.0 or below does not handle repository references inside site definition.

See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=453708
