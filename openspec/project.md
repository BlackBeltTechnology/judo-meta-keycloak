# Project Context

## Purpose

**Judo Keycloak Meta** is an Eclipse/Tycho-based metamodel project that:
- Defines a Keycloak configuration metamodel via EMF/Ecore
- Generates Java code from the model using MWE2 workflows
- Provides both Eclipse UI and OSGi standalone runtime
- Exports Keycloak configurations from model definitions
- Distributes via both Maven Central and Eclipse P2 repositories

## Tech Stack

### Core Technologies
- **Java 21** - Primary language
- **Eclipse Modeling Framework (EMF)** 2.38.0+ - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) 2.13.0 - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL) and object language (EOL)
- **Tycho** 4.0.13 - Eclipse plugin build

### Runtime
- **Apache Karaf** 4.4.7 - OSGi container
- **Apache Felix** 6.0.0 - OSGi bundle plugin

### Build & Testing
- **Maven** 3.9.4+ with wrapper
- **JUnit 5** - Unit testing
- **Pax Exam** 4.13.5 - OSGi integration testing

## Project Conventions

### Code Style
- Java 21 language features (records, pattern matching, sealed classes where applicable)
- Use Lombok for boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, `@Slf4j`)
- EMF-generated code follows GenModel conventions
- Immutable objects preferred for validation results and cache keys
- Functional interfaces for validation rules and guards

### Architecture Patterns
- **EMF/Ecore patterns** for metamodel definition and manipulation
- **Annotation-based configuration** for validation rules
- **Functional interfaces** for validation logic (lambdas supported)
- **Registry pattern** for scanning and discovering validators

### Testing Strategy
- Unit tests in `model-test/` module using JUnit 5
- EVL validation tests use `runEpsilon()` pattern
- Expected errors/warnings passed to validator for assertion
- Model fixtures created using EMF builders
- OSGi integration tests via Pax Exam in `osgi-itest/`

### Git Workflow
- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.1-SNAPSHOT)
- Feature branches for significant changes
- OpenSpec proposals for architectural changes

## Domain Context

### Keycloak Metamodel
The model defines Keycloak identity and access management configuration:
- Realms and realm settings
- Clients and client configurations
- Users, roles, and groups
- Authentication flows and providers

### Validation System
Current validation uses **Epsilon Validation Language (EVL)**:
- Rules in `model/src/main/epsilon/validations/`
- Entry point: `keycloak-plugin-validation.evl`
- Two severity levels: Constraint (ERROR) and Critique (WARNING)

## Module Overview

| Module | Purpose |
|--------|---------|
| `model/` | Core Keycloak metamodel, EMF code, Epsilon validation |
| `model-test/` | Unit tests for metamodel and validation |
| `osgi/` | OSGi bundle repackaging |
| `osgi-itest/` | OSGi integration tests |
| `feature/` | Eclipse feature packaging |
| `site/` | P2 update site |

## Active Changes

See `openspec/changes/` for in-progress proposals.
