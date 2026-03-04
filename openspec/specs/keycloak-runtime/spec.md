# keycloak-runtime Specification

## Purpose
Provides hand-written runtime utilities for Keycloak model manipulation, including stream-based querying, JSON serialization/deserialization, Epsilon EVL validation, and realm configuration export to JSON files.

## Architecture
The runtime classes live in `model/src/main/java/hu/blackbelt/judo/meta/keycloak/runtime/` and operate on EMF `ResourceSet` instances managed by the generated `KeycloakModel` container. Key classes:

- **`KeycloakModel`** (generated) — Main model container with builder/loader pattern, wraps `ResourceSet` and `Resource`
- **`KeycloakUtils`** — Stream-based querying over model elements via `all(Class<T>)`
- **`KeycloakObjectMapper`** — Jackson `ObjectMapper` configured for EMF `EList` deserialization using MixIn classes
- **`KeycloakEpsilonValidator`** — Executes Epsilon EVL validation scripts against the model
- **`KeycloakConfigurationExporter`** — Converts a `Realm` to JSON via `TreeMap` intermediary

## Requirements

### Requirement: KeycloakModel SHALL provide builder and loader patterns for model lifecycle
The model container must support both programmatic construction and loading from persistent storage.

#### Scenario: Build a new model programmatically
- **WHEN** `KeycloakModel.buildKeycloakModel()` is called and content is added via `addContent(EObject)`
- **THEN** a KeycloakModel is created with a populated `ResourceSet` and `Resource`

#### Scenario: Load a model from storage
- **GIVEN** a saved Keycloak model file
- **WHEN** `KeycloakModel.loadKeycloakModel(LoadArguments)` is called
- **THEN** the model is deserialized and a fully populated KeycloakModel is returned

#### Scenario: Save a model to storage
- **GIVEN** a populated KeycloakModel
- **WHEN** `saveKeycloakModel(SaveArguments)` is called
- **THEN** the model is serialized to the specified location; if validation fails, a `KeycloakValidationException` is thrown

### Requirement: KeycloakUtils SHALL provide type-safe stream access to model elements
The utility must allow querying all elements of a given type from the model's resource set.

#### Scenario: Query all Clients from a model
- **GIVEN** a KeycloakModel containing a Realm with multiple Clients
- **WHEN** `new KeycloakUtils(resourceSet).all(Client.class)` is called
- **THEN** a `Stream<Client>` is returned containing all Client instances in the model

#### Scenario: Query all elements of any type
- **GIVEN** a populated KeycloakModel
- **WHEN** `all()` is called (package-private)
- **THEN** a Stream of all EObject instances across all resources is returned

### Requirement: KeycloakObjectMapper SHALL deserialize JSON into EMF model elements
Jackson must be configured to handle EMF `EList` collections and map JSON to generated `*Impl` classes.

#### Scenario: Deserialize a Realm from JSON
- **GIVEN** a JSON string representing a Realm with nested clients and users
- **WHEN** `KeycloakObjectMapper.objectMapper().readValue(json, RealmImpl.class)` is called
- **THEN** the Realm is deserialized with all `EList` collections (clients, users, credentials, redirectUris) properly populated

#### Scenario: EList deserialization via MixIn classes
- **GIVEN** a JSON array field that maps to an EMF `EList`
- **WHEN** deserialization occurs
- **THEN** the custom `EListDeserializer` creates an `EList` and the MixIn collection setters (`ClientImplWithCollectionSetters`, `UserImplWithCollectionSetters`, `RealmImplWithCollectionSetters`) replace the default EList with the deserialized one

### Requirement: KeycloakEpsilonValidator SHALL validate models against EVL constraints
The validator must execute Epsilon EVL scripts and report constraint violations.

#### Scenario: Validate a correct model
- **GIVEN** a KeycloakModel containing valid Realm configuration
- **WHEN** `KeycloakEpsilonValidator.validateKeycloak(log, model, scriptURI)` is called
- **THEN** validation passes without exceptions

#### Scenario: Validate with expected errors
- **GIVEN** a KeycloakModel with known constraint violations
- **WHEN** `validateKeycloak(log, model, scriptURI, expectedErrors, expectedWarnings)` is called with matching expected collections
- **THEN** validation succeeds because the violations match expectations

#### Scenario: Calculate validation script URI
- **WHEN** `calculateKeycloakValidationScriptURI()` is called
- **THEN** the URI to `keycloak.evl` is resolved, handling JAR paths (`jar:bundle:`), classpath JARs, and filesystem locations

### Requirement: KeycloakConfigurationExporter SHALL export Realm configuration as JSON
The exporter must convert a Realm model element to a JSON representation suitable for Keycloak server import.

#### Scenario: Export realm to JSON string
- **GIVEN** a Realm with id, realm name, clients, and users
- **WHEN** `new KeycloakConfigurationExporter(realm).getConfigurationAsString()` is called
- **THEN** a JSON string is returned with alphabetically sorted keys and only non-null attributes included

#### Scenario: Export realm to file
- **GIVEN** a Realm and a target File
- **WHEN** `writeConfigurationToFile(file)` is called
- **THEN** the JSON representation is written to the specified file

#### Scenario: Client export includes all OAuth2 attributes
- **GIVEN** a Realm with a Client having `clientId`, `secret`, `publicClient`, `bearerOnly`, `directAccessGrantsEnabled`, and `redirectUris`
- **WHEN** the realm is exported
- **THEN** the JSON output contains all non-null Client attributes in the `clients` array
