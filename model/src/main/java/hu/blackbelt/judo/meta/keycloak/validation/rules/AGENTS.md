# `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/rules` — annotated rule bodies the zeta registry discovers and executes

| File | Purpose |
|---|---|
| `KeycloakValidations.java` | Declares every Java-side rule mirroring `keycloak.evl`. `@ValidationContext(EObject.class)`; each public no-arg method returns a `ValidationRule` and is annotated `@Constraint` (error) or `@Critique` (warning): `realmNameNotEmpty`, `realmIdNotEmpty`, `clientIdNotEmpty`, `clientInternalIdNotEmpty`, `clientNameNotEmpty`, `userUsernameNotEmpty`, `userEmailValidFormat`, `userCredentialTypeNotEmpty`, `userCredentialValueNotEmpty`, `attributeBindingNameNotEmpty`. Because the context is `EObject`, every rule must itself `instanceof`-guard and return `ValidationResult.pass()` for foreign types. Names come from `KeycloakValidationConstants`; `isValidEmail` uses the same regex as the EVL critique, and blank email never warns. |
