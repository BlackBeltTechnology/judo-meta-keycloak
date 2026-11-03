# Keycloak Model Validation

This document describes the validation framework for the Keycloak metamodel.

## Overview

The Keycloak metamodel supports two validation engines that run in parallel:

1. **EVL (Epsilon Validation Language)** - Traditional script-based validation
2. **Java (Zeta Framework)** - Native Java validation with annotations

Both validators produce identical results for the same model, ensuring consistency while providing different trade-offs for development and runtime performance.

## Architecture

```mermaid
graph TB
    subgraph "Validation Entry Points"
        EVL[KeycloakEpsilonValidator]
        Java[KeycloakValidator]
    end

    subgraph "Validation Engine"
        EVL --> EvlScript[keycloak.evl]
        Java --> Registry[ValidationRegistry]
        Registry --> Rules[KeycloakValidations]
    end

    subgraph "Model"
        EvlScript --> Model[KeycloakModel]
        Rules --> Model
    end

    subgraph "Results"
        Model --> Errors[Errors]
        Model --> Warnings[Warnings]
    end
```

## Validation Rules

### Realm Validations

| Constraint | Severity | Description |
|------------|----------|-------------|
| `RealmNameNotEmpty` | ERROR | Realm must have a non-empty realm name |
| `RealmIdNotEmpty` | ERROR | Realm must have a non-empty ID |

### Client Validations

| Constraint | Severity | Description |
|------------|----------|-------------|
| `ClientIdNotEmpty` | ERROR | Client must have a non-empty clientId |
| `ClientInternalIdNotEmpty` | ERROR | Client must have a non-empty internal ID |
| `ClientNameNotEmpty` | WARNING | Client should have a name for identification |

### User Validations

| Constraint | Severity | Description |
|------------|----------|-------------|
| `UserUsernameNotEmpty` | ERROR | User must have a non-empty username |
| `UserEmailValidFormat` | WARNING | User email should have valid format |

### UserCredential Validations

| Constraint | Severity | Description |
|------------|----------|-------------|
| `UserCredentialTypeNotEmpty` | ERROR | UserCredential must have a type |
| `UserCredentialValueNotEmpty` | ERROR | UserCredential must have a value |

### AttributeBinding Validations

| Constraint | Severity | Description |
|------------|----------|-------------|
| `AttributeBindingNameNotEmpty` | ERROR | AttributeBinding must have an attributeName |

## Usage

### EVL Validation

```java
KeycloakEpsilonValidator.validateKeycloak(
    log,
    keycloakModel,
    KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI(),
    expectedErrors,
    expectedWarnings
);
```

### Java Validation

```java
KeycloakValidator.validateKeycloak(
    log,
    keycloakModel,
    expectedErrors,
    expectedWarnings
);
```

### Parallel Validation (for performance)

```java
KeycloakValidator.validateKeycloak(
    log,
    keycloakModel,
    expectedErrors,
    expectedWarnings,
    true  // parallel=true
);
```

## Testing

Both validators are tested using the same test cases via JUnit 5 parameterized tests:

```java
@ParameterizedTest(name = "testRealmNameNotEmpty [{0}]")
@EnumSource(ValidatorType.class)
void testRealmNameNotEmpty(ValidatorType type) throws Exception {
    this.validatorType = type;
    initModel();

    Realm realm = keycloakModelSupport.newRealmBuilder()
        .withId("test-id")
        .withRealm("")  // Empty - should trigger error
        .build();
    keycloakModel.addContent(realm);

    runValidation(
        ImmutableList.of("RealmNameNotEmpty"),
        ImmutableList.of()
    );
}
```

## Performance

Java validation is significantly faster than EVL, especially for large models:

- **Sequential Java**: 5-10x faster than EVL
- **Parallel Java**: Additional speedup on multi-core systems

Run performance benchmarks:

```bash
mvn test -Dtest=KeycloakValidationPerformanceTest -Dperformance.test=true
```

## Zeta Framework

The Java validation is built on the Zeta Validation Framework.

For detailed framework documentation, see:
- [Zeta Framework Repository](https://github.com/BlackBeltTechnology/judo-zeta)
- [Zeta Getting Started Guide](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/getting-started.md)
- [Zeta User Guide](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/)

### Key Annotations

| Annotation | EVL Equivalent | Purpose |
|------------|----------------|---------|
| `@ValidationContext(Class)` | `context Type` | Target EClass for rules |
| `@Constraint` | `constraint` | Error-level rule |
| `@Critique` | `critique` | Warning-level rule |
| `@Guard` | `guard:` | Conditional execution |
| `@Satisfies` | `satisfies()` | Constraint dependency |

## Adding New Validation Rules

1. Add constant to `KeycloakValidationConstants.java`
2. Add rule method to `KeycloakValidations.java` with appropriate annotation
3. Add corresponding EVL rule to `keycloak.evl` (for parity)
4. Add test case to `KeycloakValidationTest.java`
5. Update this documentation

Example:

```java
// In KeycloakValidationConstants.java
public static final String MY_NEW_CONSTRAINT = "MyNewConstraint";
public static final String MSG_MY_NEW_CONSTRAINT = "Element must satisfy condition";

// In KeycloakValidations.java
@Constraint(
    name = MY_NEW_CONSTRAINT,
    description = "Element must satisfy condition",
    message = "Element must satisfy condition"
)
public ValidationRule myNewConstraint() {
    return (element, ctx) -> {
        if (!(element instanceof MyType)) {
            return ValidationResult.pass();
        }
        MyType self = (MyType) element;
        if (!self.satisfiesCondition()) {
            return ValidationResult.fail(
                MY_NEW_CONSTRAINT,
                MSG_MY_NEW_CONSTRAINT,
                Severity.ERROR,
                self
            );
        }
        return ValidationResult.pass();
    };
}
```
