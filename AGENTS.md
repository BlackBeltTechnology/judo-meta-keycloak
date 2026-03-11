<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# Judo Keycloak Meta - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-keycloak
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with Tycho (Eclipse build tooling)

This is an Eclipse/Tycho-based metamodel project that:
1. **Defines** a Keycloak configuration metamodel via EMF/Ecore
2. **Generates** Java code from the model using MWE2 workflows
3. **Provides** both Eclipse UI and OSGi standalone runtime
4. **Exports** Keycloak configurations from model definitions
5. **Distributes** via both Maven Central and Eclipse P2 repositories

## Directory Structure

```
judo-meta-keycloak/
├── model/                          # Core Keycloak metamodel (Ecore)
│   ├── src/main/epsilon/           # EVL validation scripts
│   └── src/main/java/              # Java validation framework
├── model-test/                     # Unit tests for metamodel
├── osgi/                           # OSGi bundle repackaging
├── osgi-itest/                     # OSGi integration tests (Pax Exam)
├── feature/                        # Eclipse feature (model)
├── site/                           # Eclipse P2 update site
├── targetdefinition/               # P2 repository definitions
├── docs/                           # Documentation
│   └── validation/                 # Validation framework docs
└── openspec/                       # OpenSpec change management
```

## Keycloak Metamodel

The core metamodel (`model/model/keycloak.ecore`) defines:

| EClass | Purpose |
|--------|---------|
| `Realm` | Keycloak realm configuration (contains clients and users) |
| `Client` | Application/actor configuration |
| `User` | User account configuration |
| `UserCredential` | User credentials (password, etc.) |
| `AttributeBinding` | Mapping of actor attributes to Keycloak attributes |

## Validation Framework

The project supports **dual validation** with both EVL and Java validators running in parallel.

### EVL Validation
- Location: `model/src/main/epsilon/validations/keycloak*.evl`
- Entry point: `KeycloakEpsilonValidator.validateKeycloak()`

### Java Validation (Zeta Framework)
- Location: `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/`
- Entry point: `KeycloakValidator.validateKeycloak()`
- Uses [Zeta Validation Framework](https://github.com/BlackBeltTechnology/judo-zeta)

### Key Validation Classes

| Class | Purpose |
|-------|---------|
| `KeycloakValidator` | Entry point for Java validation |
| `KeycloakValidationConstants` | Constraint names and message templates |
| `KeycloakValidations` | Validation rules with @Constraint/@Critique annotations |
| `KeycloakValidationException` | Exception for validation failures |

### Validation Rules

| Constraint | Target | Severity |
|------------|--------|----------|
| `RealmNameNotEmpty` | Realm | ERROR |
| `RealmIdNotEmpty` | Realm | ERROR |
| `ClientIdNotEmpty` | Client | ERROR |
| `ClientInternalIdNotEmpty` | Client | ERROR |
| `ClientNameNotEmpty` | Client | WARNING |
| `UserUsernameNotEmpty` | User | ERROR |
| `UserEmailValidFormat` | User | WARNING |
| `UserCredentialTypeNotEmpty` | UserCredential | ERROR |
| `UserCredentialValueNotEmpty` | UserCredential | ERROR |
| `AttributeBindingNameNotEmpty` | AttributeBinding | ERROR |

## Technology Stack

### Core Technologies
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL)
- **Tycho** 4.0.13 - Eclipse plugin build
- **Zeta** 1.0.0-SNAPSHOT - Java validation framework

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin
- **Pax Exam** 4.13.5 - OSGi testing

## Build Commands

```bash
# Standard build
mvn clean install
# or with wrapper
./mvnw clean install

# Run tests
mvn test

# Run performance tests
mvn test -Dtest=KeycloakValidationPerformanceTest -Dperformance.test=true
```

## Testing

### Parametrized Validation Tests

Tests run with both EVL and Java validators using `@EnumSource(ValidatorType.class)`:

```java
@ParameterizedTest(name = "testRealmNameNotEmpty [{0}]")
@EnumSource(ValidatorType.class)
void testRealmNameNotEmpty(ValidatorType type) throws Exception {
    this.validatorType = type;
    initModel();
    // ... test code
    runValidation(expectedErrors, expectedWarnings);
}
```

### Test Classes

| Class | Purpose |
|-------|---------|
| `AbstractKeycloakValidationTest` | Base class with validation infrastructure |
| `KeycloakValidationTest` | Parametrized validation tests |
| `KeycloakValidationPerformanceTest` | Performance benchmarks |
| `ValidatorType` | Enum for selecting EVL or JAVA validator |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+
- Eclipse IDE with:
  - m2e (Maven integration)
  - Epsilon plugin
  - Modeling tools

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.1-SNAPSHOT)

## Related Documentation

- `README.md` - Project overview
- `docs/validation/README.md` - Validation framework documentation
- `openspec/AGENTS.md` - OpenSpec workflow for spec-driven development
- `openspec/project.md` - Project conventions for OpenSpec
- [Zeta Framework](https://github.com/BlackBeltTechnology/judo-zeta) - Java validation framework
