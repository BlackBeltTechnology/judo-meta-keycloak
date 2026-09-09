# `model/src/main/java/hu/blackbelt/judo/meta/keycloak/runtime/exporter` — renders a `Realm` into Keycloak's own realm-import JSON

| File | Purpose |
|---|---|
| `KeycloakConfigurationExporter.java` | Converts one `Realm` into the JSON shape Keycloak's realm import accepts. Exports `KeycloakConfigurationExporter(Realm)`, `getConfigurationAsString()`, `writeConfigurationToFile(File)`, `writeConfigurationToStream(OutputStream)`. Serialization is hand-mapped through `TreeMap`, not EMF reflection: only `id`, `realm`, `enabled`, `loginWithEmailAllowed` and `clients` (`id`, `clientId`, `name`, `enabled`, `redirectUris`, `bearerOnly`, `publicClient`, `directAccessGrantsEnabled`, `secret`) are emitted — users, credentials, attribute bindings and any new metamodel attribute are silently dropped until added here. Null-valued attributes are omitted; every failure is rethrown as `IllegalStateException`. |
