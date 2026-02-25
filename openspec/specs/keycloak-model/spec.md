# keycloak-model Specification

## Purpose
Defines the Ecore metamodel for Keycloak IAM configuration and generates EMF Java code (interfaces, implementations, factory, package descriptor, builders, helpers, and runtime model support) via MWE2 workflows.

## Architecture
The metamodel is defined in `model/model/keycloak.ecore` with five entity types forming a containment hierarchy: `Realm` → `Client` (with `AttributeBinding`) and `Realm` → `User` (with `UserCredential`). The `keycloak.genmodel` descriptor controls Java code generation parameters. The MWE2 workflow (`model/src/workflow/generateModel.mwe2`) orchestrates four generators: `EcoreGenerator`, `HelperGeneratorWorkflow`, `BuilderGeneratorWorkflow`, and `RuntimeModelGeneratorWorkflow`, producing output to `src-gen/`.

### Entity Attributes

| Entity | Key Attributes |
|--------|---------------|
| `Realm` | `id` (EString, ID), `realm` (EString), `enabled` (EBooleanObject), `loginWithEmailAllowed` (EBooleanObject) |
| `Client` | `id` (EString, ID), `clientId`, `name`, `secret`, `enabled`, `publicClient`, `bearerOnly`, `directAccessGrantsEnabled` (all EBooleanObject), `redirectUris` (EString[]), `webOrigins` (EString[]) |
| `User` | `username`, `email`, `firstName`, `lastName` (EString), `enabled` (EBooleanObject), `requiredActions` (EString[]) |
| `UserCredential` | `type`, `value` (EString), `temporary` (EBooleanObject) |
| `AttributeBinding` | `attributeName` (EString) |

### Containment References

| Parent | Reference | Target | Cardinality |
|--------|-----------|--------|-------------|
| `Realm` | `clients` | `Client` | 0..* |
| `Realm` | `users` | `User` | 0..* |
| `Client` | `attributeBindings` | `AttributeBinding` | 0..* |
| `User` | `credentials` | `UserCredential` | 0..* |

## Requirements

### Requirement: Realm SHALL be the root container for all Keycloak configuration
A Realm serves as the top-level containment element, owning collections of clients and users.

#### Scenario: Create a realm with clients and users
- **GIVEN** an empty KeycloakModel
- **WHEN** a Realm is created with `id`, `realm`, `enabled` attributes and child Client and User elements
- **THEN** the Realm contains all clients via the `clients` containment reference and all users via the `users` containment reference

### Requirement: Client SHALL model OAuth2/SAML client applications
A Client represents an application registered in Keycloak with its authentication configuration.

#### Scenario: Create a public client with redirect URIs
- **GIVEN** a Realm
- **WHEN** a Client is added with `publicClient=true` and one or more `redirectUris`
- **THEN** the Client is contained within the Realm's `clients` reference with all attributes set

#### Scenario: Create a confidential client with secret
- **GIVEN** a Realm
- **WHEN** a Client is added with `publicClient=false`, `bearerOnly=false`, and a `secret` value
- **THEN** the Client is contained within the Realm and exposes its `secret` attribute

### Requirement: User SHALL model realm users with credentials
A User represents a person who can authenticate against the realm.

#### Scenario: Create a user with password credential
- **GIVEN** a Realm
- **WHEN** a User is added with `username`, `email`, and a UserCredential of `type=password`
- **THEN** the User is contained in the Realm's `users` reference and the credential is contained in the User's `credentials` reference

### Requirement: Code generation SHALL produce complete EMF artifacts
The MWE2 workflow must generate all required Java classes from the Ecore model.

#### Scenario: Run MWE2 code generation
- **GIVEN** a valid `keycloak.ecore` and `keycloak.genmodel`
- **WHEN** the MWE2 workflow `generateModel.mwe2` executes
- **THEN** `src-gen/` contains EMF interfaces, `*Impl` classes, `KeycloakFactory`, `KeycloakPackage`, builder classes, helper classes, and `KeycloakModel`/`KeycloakModelResourceSupport` runtime support classes

### Requirement: Builder pattern SHALL be generated for all entity types
Generated builders enable fluent construction of model elements.

#### Scenario: Build a Realm using KeycloakBuilders
- **WHEN** `KeycloakBuilders.newRealmBuilder().withId("test").withRealm("test").withClients(...).build()` is called
- **THEN** a fully populated Realm instance is returned with all specified attributes and contained children
