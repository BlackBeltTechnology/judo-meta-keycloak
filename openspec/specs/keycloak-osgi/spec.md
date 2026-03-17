# keycloak-osgi Specification

## Purpose
Packages the Keycloak metamodel as a standalone OSGi bundle for non-Eclipse environments (Apache Karaf) and provides automatic discovery and registration of Keycloak model instances from deployed bundles.

## Architecture
The `osgi` module repackages the `model` artifact using Apache Felix Maven Bundle Plugin, embedding Epsilon validation rules as bundle resources. The key component is `KeycloakModelBundleTracker`, an OSGi Declarative Services component that tracks bundles with a `Keycloak-Models` manifest header and automatically loads and registers `KeycloakModel` instances as OSGi services.

### Key Classes

- **`KeycloakModelBundleTracker`** — `@Component(immediate=true)` that uses `BundleTrackerManager` to discover model-bearing bundles
- **`KeycloakBundlePredicate`** (inner class) — Tests whether a bundle has the `Keycloak-Models` header
- **`KeycloakRegisterCallback`** (inner class) — Loads model and registers OSGi service on bundle arrival
- **`KeycloakUnregisterCallback`** (inner class) — Unregisters service and cleans up on bundle removal

## Requirements

### Requirement: The bundle SHALL export all Keycloak model packages
The OSGi bundle must make all `hu.blackbelt.judo.meta.keycloak.*` packages available to other bundles.

#### Scenario: Import Keycloak packages from another bundle
- **GIVEN** the `hu.blackbelt.judo.meta.keycloak.osgi` bundle is active in a Karaf container
- **WHEN** another bundle declares `Import-Package: hu.blackbelt.judo.meta.keycloak`
- **THEN** the package is resolved and the Keycloak model classes are available

### Requirement: The bundle SHALL embed Epsilon validation rules
EVL validation scripts must be packaged as bundle resources so validation works without filesystem access.

#### Scenario: Run validation in OSGi environment
- **GIVEN** the OSGi bundle is active
- **WHEN** `KeycloakEpsilonValidator.calculateKeycloakValidationScriptURI()` is called
- **THEN** the URI resolves to the embedded `keycloak.evl` resource within the bundle

### Requirement: KeycloakModelBundleTracker SHALL auto-discover model bundles
Bundles with a `Keycloak-Models` manifest header must be automatically detected, loaded, and registered as OSGi services.

#### Scenario: Deploy a bundle with Keycloak model
- **GIVEN** the tracker component is active
- **WHEN** a bundle with header `Keycloak-Models: name=northwind;file=model/northwind-keycloak.model` transitions to ACTIVE state
- **THEN** the tracker loads the model via `KeycloakModel.loadKeycloakModel()`, registers it as a `KeycloakModel` OSGi service, and stores the registration in its internal map

#### Scenario: Undeploy a model bundle
- **GIVEN** a registered Keycloak model service from a tracked bundle
- **WHEN** the source bundle is stopped or uninstalled
- **THEN** the `KeycloakUnregisterCallback` unregisters the OSGi service and removes the model from internal maps

#### Scenario: Bundle without Keycloak-Models header is ignored
- **GIVEN** the tracker component is active
- **WHEN** a bundle without a `Keycloak-Models` header is deployed
- **THEN** `KeycloakBundlePredicate.test()` returns false and the bundle is ignored

### Requirement: Model services SHALL expose model properties as service properties
The registered OSGi service must carry metadata from the model for service lookups.

#### Scenario: Look up model by name
- **GIVEN** a registered Keycloak model service with name "northwind"
- **WHEN** a service lookup filters on `(name=northwind)`
- **THEN** the correct `KeycloakModel` service is returned
