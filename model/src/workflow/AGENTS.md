# `model/src/workflow` — MWE2 workflow driving EMF code generation for the Keycloak metamodel

| File | Purpose |
|---|---|
| `generateModel.mwe2` | Regenerates `src-gen` from `model/keycloak.genmodel` on every build. Declares workflow `KeycloakModelBuilder`, vars `rootPath="."`, `modelDir="model"`, `javaGenPath="src-gen"`, `platformUri="platform:/resource/hu.blackbelt.judo.meta.keycloak.model"`, and chains `StandaloneSetup` → `DirectoryCleaner` → `EcoreGenerator` → `HelperGeneratorWorkflow` → `BuilderGeneratorWorkflow` → `RuntimeModelGeneratorWorkflow`. `DirectoryCleaner` wipes `src-gen` first, so hand-written code placed there is destroyed; `platformUri` must match the plugin's project name or generation fails to resolve the genmodel. |
