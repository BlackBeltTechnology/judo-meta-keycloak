# Keycloak Validation Capability

## Overview

Model validation for Keycloak metamodel elements, ensuring model correctness before processing.

---

## ADDED Requirements

### Requirement: Java-Based Validation Framework

The system shall provide native Java-based validation using the Zeta framework as an alternative to EVL.

#### Scenario: Java validation produces same results as EVL
Given a Keycloak model with validation errors
When the model is validated using Java validator
Then the validation results match EVL validation results exactly

#### Scenario: Java validation runs faster than EVL
Given a large Keycloak model with 10,000+ elements
When the model is validated using Java validator
Then the Java validation completes in less time than EVL validation

#### Scenario: Java validation supports parallel execution
Given a large Keycloak model
When the model is validated using Java validator with parallel=true
Then multiple validation rules execute concurrently

### Requirement: Validation Rule Constants

All validation rule names, message templates, and guard method names shall be defined as constants.

#### Scenario: Constraint names use constants
Given a validation rule definition
When the constraint name is specified
Then it references a constant from KeycloakValidationConstants class

#### Scenario: No inline constraint name strings
Given the validation framework codebase
When searching for @Constraint annotations
Then all name parameters reference constants, not inline strings

### Requirement: Parametrized Validation Tests

Validation tests shall run with both EVL and Java validators using the same test cases.

#### Scenario: Test runs with EVL validator
Given a validation test case
When executed with ValidatorType.EVL
Then the test uses KeycloakEpsilonValidator

#### Scenario: Test runs with Java validator
Given a validation test case
When executed with ValidatorType.JAVA
Then the test uses KeycloakValidator

#### Scenario: Both validators produce same test results
Given a model with specific validation errors
When validated by both EVL and Java validators
Then both report the same constraint violations

### Requirement: Performance Benchmarking

The system shall provide performance tests comparing EVL and Java validation.

#### Scenario: Benchmark with synthetic model
Given a programmatically generated model with 10,000 elements
When performance test executes
Then timing results are reported for EVL, Java sequential, and Java parallel

---

## ADDED Documentation Requirements

### Requirement: Validation Documentation

Comprehensive documentation shall be provided for the validation framework.

#### Scenario: Validation overview documentation
Given a new developer
When they access docs/validation/README.md
Then they understand the dual-validation approach

#### Scenario: Validation rule documentation
Given a validation rule
When documented
Then it includes: name, description, target type, severity, and example

#### Scenario: Zeta framework references
Given the documentation
When referencing Zeta framework details
Then it links to external Zeta documentation rather than copying

### Requirement: Documentation Format Migration

Documentation shall be converted from AsciiDoc to Markdown.

#### Scenario: README conversion
Given README.adoc
When converted
Then README.md contains equivalent content in Markdown format

#### Scenario: PlantUML to Mermaid conversion
Given a PlantUML diagram
When converted
Then equivalent Mermaid diagram is created

#### Scenario: Pages directory excluded
Given files under pages/ directory
When documentation conversion runs
Then pages/ files are not modified

---

## Technical Notes

### Zeta Framework Integration

The validation framework uses Zeta annotations:

| Annotation | EVL Equivalent | Purpose |
|------------|----------------|---------|
| `@ValidationContext(Class)` | `context Type` | Target EClass for rules |
| `@Constraint` | `constraint` | Error-level rule |
| `@Critique` | `critique` | Warning-level rule |
| `@Guard` | `guard:` | Conditional execution |
| `@Satisfies` | `satisfies()` | Constraint dependency |

### Package Structure

```
hu.blackbelt.judo.meta.keycloak.validation/
├── KeycloakValidator.java
├── KeycloakValidationConstants.java
├── rules/
│   └── KeycloakValidations.java
└── extensions/
    └── KeycloakElementExtensions.java
```

### Dependencies

- `hu.blackbelt.judo.zeta:validation-core:1.0.0-SNAPSHOT`
- `hu.blackbelt.judo.zeta:zeta-annotations:1.0.0-SNAPSHOT`
