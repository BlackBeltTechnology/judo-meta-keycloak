# `model/src/main/java/hu/blackbelt/judo/eclipse/keycloak` — Eclipse plugin lifecycle entry point for the metamodel bundle

| File | Purpose |
|---|---|
| `Activator.java` | Holds the bundle's singleton across the Eclipse plugin lifecycle. Extends `AbstractUIPlugin`; exports `PLUGIN_ID = "hu.blackbelt.judo.meta.keycloak"`, `start(BundleContext)`, `stop(BundleContext)`, `getDefault()`. `getDefault()` returns `null` before `start` and after `stop`, so callers outside an active Eclipse runtime must not dereference it. `PLUGIN_ID` must match the `Bundle-SymbolicName` in `META-INF/MANIFEST.MF`. |
