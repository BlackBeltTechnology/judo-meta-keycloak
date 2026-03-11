# keycloak-testing Specification

## Purpose
Validates the correctness of the Keycloak metamodel, runtime utilities, and OSGi integration through unit tests (JUnit 5) and container integration tests (Pax Exam on Karaf).

## Architecture
Tests are split across two modules:

- **`model-test/`** — JUnit 5 unit tests covering `KeycloakUtils`, `KeycloakConfigurationExporter`, `KeycloakEpsilonValidator`, and `KeycloakModel` lifecycle. Tests use the generated builder pattern (`KeycloakBuilders`) to construct test data.
- **`osgi-itest/`** — Pax Exam integration tests running inside an Apache Karaf 4.4.7 container. Tests verify bundle resolution, model loading from bundle entries, and OSGi service registration.

### Test Classes

| Class | Module | Framework | Tests |
|-------|--------|-----------|-------|
| `KeycloakUtilsTest` | model-test | JUnit 5 | Stream operations via `KeycloakUtils.all(Class)` |
| `KeycloakConfigurationExporterTest` | model-test | JUnit 5 | JSON export to string and file with real Realm/Client structures |
| `KeycloakValidationTest` | model-test | JUnit 5 | Epsilon EVL validation execution |
| `KeycloakExecutionContextTest` | model-test | JUnit 5 | `KeycloakModel` builder pattern and execution context setup |
| `KeycloakModelLoadITest` | osgi-itest | Pax Exam / JUnit 4 | Model loading in Karaf, dynamic bundle creation, service injection, validation in OSGi context |

## Requirements

### Requirement: Unit tests SHALL verify model element querying
KeycloakUtils stream operations must correctly return all elements of a requested type.

#### Scenario: Query all elements of a specific type
- **GIVEN** a KeycloakModel built with `KeycloakBuilders.newRealmBuilder()` containing multiple clients and users
- **WHEN** `new KeycloakUtils(resourceSet).all(Client.class)` is invoked
- **THEN** the returned stream contains exactly the Client instances that were added to the model

### Requirement: Unit tests SHALL verify JSON export correctness
The exporter must produce valid JSON with correct structure and attribute values.

#### Scenario: Export realm with clients to JSON
- **GIVEN** a Realm built with `newRealmBuilder().withId("sandbox").withRealm("sandbox").withClients(newClientBuilder().withClientId("Northwind-Internal").build()).build()`
- **WHEN** `new KeycloakConfigurationExporter(realm).getConfigurationAsString()` is called
- **THEN** the JSON string contains `id`, `realm`, `clients` array with `clientId` field

#### Scenario: Export realm to file
- **GIVEN** a Realm and a temporary File
- **WHEN** `writeConfigurationToFile(file)` is called
- **THEN** the file contains valid JSON and is non-empty

### Requirement: Unit tests SHALL verify Epsilon validation execution
The validator must successfully run EVL scripts against valid models.

#### Scenario: Validate a well-formed model
- **GIVEN** a KeycloakModel with a valid Realm
- **WHEN** `KeycloakEpsilonValidator.validateKeycloak(log, model, calculateKeycloakValidationScriptURI())` is called
- **THEN** no `ScriptExecutionException` is thrown

### Requirement: OSGi integration tests SHALL verify model loading in Karaf
Models must be loadable and usable inside an OSGi container environment.

#### Scenario: Load model from bundle in Karaf
- **GIVEN** a Karaf container with the Keycloak OSGi bundle installed
- **WHEN** a test bundle with a `Keycloak-Models` header is deployed dynamically
- **THEN** the `KeycloakModel` is loaded, registered as an OSGi service, and validation succeeds within the container
