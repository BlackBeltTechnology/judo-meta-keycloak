# Design: Zeta Validation Framework Integration

## Context

The Keycloak metamodel currently uses Epsilon Validation Language (EVL) for model validation. While EVL is powerful, it has limitations:
- Interpreted at runtime (slower)
- Limited IDE support
- Difficult to debug
- Non-standard syntax

The ESM module has successfully implemented a dual-validation approach using the Zeta framework, allowing both EVL and native Java validation to run in parallel.

## Decision

Implement Java-based validation using the Zeta framework, following the ESM module's established patterns:

### Package Structure

```
model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/
├── KeycloakValidator.java              # Entry point
├── KeycloakValidationConstants.java    # Constants for constraint names
├── annotation/                         # (Imported from zeta-annotations)
├── core/                              # (Imported from validation-core)
├── extensions/                        # Extension methods
│   └── KeycloakElementExtensions.java
└── rules/                             # Validation rules by category
    └── KeycloakValidations.java       # All Keycloak rules

model-test/src/test/java/hu/blackbelt/judo/meta/keycloak/
├── ValidatorType.java                  # EVL/JAVA enum
├── AbstractKeycloakValidationTest.java # Base test class
├── KeycloakValidationTest.java         # Parametrized validation tests
└── KeycloakValidationPerformanceTest.java # Performance benchmarks
```

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    KeycloakValidator                            │
│                      (Entry Point)                              │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              ValidationRegistry (from Zeta)                     │
│  - Scans @ValidationContext classes                             │
│  - Registers @Constraint/@Critique methods                      │
│  - Manages ValidatorDescriptors                                 │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│             ValidationExecutor (from Zeta)                      │
│  - Sequential or parallel execution                             │
│  - Iterates elements and validators                             │
│  - Collects ValidationResults                                   │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│             ValidatorDescriptor (from Zeta)                     │
│  - Wraps a single validation rule                               │
│  - Handles @Guard evaluation                                    │
│  - Handles @Satisfies dependency checks                         │
└─────────────────────────────────────────────────────────────────┘
```

### ValidatorType Enum

```java
/**
 * Enum selector for choosing which validation engine to use in tests.
 */
public enum ValidatorType {
    /**
     * Use EVL (Epsilon Validation Language) based validation.
     * Executed via KeycloakEpsilonValidator.
     */
    EVL,

    /**
     * Use native Java-based validation.
     * Executed via KeycloakValidator.
     */
    JAVA
}
```

### Validation Rule Pattern

```java
@ValidationContext(Realm.class)
public class KeycloakValidations {

    @Constraint(name = KeycloakValidationConstants.REALM_NAME_NOT_EMPTY,
                message = "Realm name must not be empty")
    public ValidationRule realmNameNotEmpty() {
        return (element, ctx) -> {
            Realm self = (Realm) element;
            if (self.getName() == null || self.getName().isEmpty()) {
                return ValidationResult.fail(
                    KeycloakValidationConstants.REALM_NAME_NOT_EMPTY,
                    "Realm: " + self.getName() + " must have a non-empty name",
                    Severity.ERROR,
                    self
                );
            }
            return ValidationResult.pass();
        };
    }
}
```

### Constants Pattern

```java
public final class KeycloakValidationConstants {
    private KeycloakValidationConstants() {}

    // Constraint names
    public static final String REALM_NAME_NOT_EMPTY = "RealmNameNotEmpty";
    public static final String CLIENT_ID_NOT_EMPTY = "ClientIdNotEmpty";
    // ... more constants

    // Guard method names
    public static final String GUARD_HAS_CONTAINER = "hasContainer";

    // Message templates
    public static final String MSG_REALM_NAME_EMPTY = "Realm: %s must have a non-empty name";
}
```

### Test Base Class Pattern

```java
public abstract class AbstractKeycloakValidationTest {

    protected KeycloakModel keycloakModel;
    protected ValidatorType validatorType;

    protected void initModel() {
        keycloakModel = KeycloakModel.buildKeycloakModel()
            .uri(URI.createURI("urn:keycloak.test"))
            .build();
    }

    protected void runValidation(
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws Exception {
        switch (validatorType) {
            case EVL:
                runEvlValidation(expectedErrors, expectedWarnings);
                break;
            case JAVA:
                KeycloakValidator.validateKeycloak(
                    log, keycloakModel, expectedErrors, expectedWarnings);
                break;
        }
    }

    // ... EVL validation helper
}
```

### Parametrized Test Pattern

```java
class KeycloakValidationTest extends AbstractKeycloakValidationTest {

    @ParameterizedTest(name = "testRealmNameNotEmpty [{0}]")
    @EnumSource(ValidatorType.class)
    void testRealmNameNotEmpty(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Build model with empty realm name
        Realm realm = newRealmBuilder().withName("").build();
        keycloakModel.addContent(realm);

        runValidation(
            ImmutableList.of(KeycloakValidationConstants.REALM_NAME_NOT_EMPTY),
            ImmutableList.of()
        );
    }
}
```

## Alternatives Considered

### 1. Use Epsilon EOL Operations Instead
**Rejected**: Would require Epsilon runtime, losing most performance benefits.

### 2. Copy Zeta Framework Code
**Rejected**: Maintenance burden, divergence risk. Better to reference via dependency.

### 3. Implement Custom Validation Framework
**Rejected**: Zeta already exists and is proven in ESM module.

### 4. Only Java Validation (Remove EVL)
**Rejected**: Gradual migration with parallel validation ensures correctness.

## Consequences

### Positive
- 5-10x faster validation for large models
- Full IDE support for validation development
- Compile-time type safety
- Consistent with ESM module approach
- Easy to debug validation logic

### Negative
- Dual maintenance during transition (EVL + Java)
- Additional dependency (Zeta framework)
- Need to keep rule implementations synchronized

### Neutral
- Learning curve for Zeta annotations (minimal, similar to EVL concepts)
- Additional test infrastructure (necessary for correctness)
