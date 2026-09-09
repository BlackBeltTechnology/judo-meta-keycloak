# `osgi-itest/src/test/resources` — Karaf feature repository provisioned into the Pax Exam container

| File | Purpose |
|---|---|
| `test-features.xml` | Declares the single Karaf feature the ITest container installs. Feature repository `judo-TEST` (features XSD v1.5.0) exports feature `test` version `1`, referenced by name from `KarafFeatureProvider.karafConfig` through `target/test-classes/test-features.xml`. Prerequisite features `wrap`, `shell`, `scr`, then `osgi-utils`, `tinybundles`, `epsilon-runtime`, the three `hu.blackbelt.judo.zeta` bundles (`annotations`, `common`, `validation-core`) and the CXF JAX-RS/Jackson/Swagger2 features. Every `${...}-version` placeholder is filtered by the build, so the file only resolves from `target/test-classes`, never from `src`. Dropping the zeta bundles breaks `KeycloakModelLoadITest.testZetaModelValidation`; dropping `tinybundles` breaks the run-time model bundle build. |
