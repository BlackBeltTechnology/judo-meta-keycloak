# `osgi/src/main/java/hu/blackbelt/judo/meta/keycloak/osgi` — OSGi bundle tracker publishing Keycloak models carried by other bundles

| File | Purpose |
|---|---|
| `KeycloakModelBundleTracker.java` | Turns a `Keycloak-Models` manifest header into live `KeycloakModel` OSGi services. Immediate `@Component`; exports constant `KEYCLOAK_MODELS`, `activate(ComponentContext)`, `deactivate(ComponentContext)`. Registers register/unregister `BundleCallback` pairs on the injected `BundleTrackerManager`, keyed by class name. Each header entry needs `name` and `file` params; `file` is opened via `Bundle.getEntry` and the bundle version becomes the model version. Registration is keyed by model name — a second bundle with the same name is logged as an error and never registered, and `IOException` / `KeycloakValidationException` are logged, not rethrown, so a bad model silently yields no service. |
