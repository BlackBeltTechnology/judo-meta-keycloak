# judo-meta-keycloak — module agent doctrine

## Module purpose

`judo-meta-keycloak` is the estate's authority on *what an authentication realm
looks like* for a generated JUDO application: it owns the `keycloak` Ecore
metamodel (`nsURI http://blackbelt.hu/judo/meta/keycloak`, classifiers `Realm`,
`Client`, `User`, `UserCredential`, `AttributeBinding`), so a realm — its
clients, their grant flags and secrets, its seeded users and credentials, and
the bindings that map JUDO actor attributes onto Keycloak user attributes — is
expressed as a typed model instance rather than a hand-maintained JSON blob.
Upstream generators build a `Realm` graph from the actor definitions of a JUDO
model; this module then validates it (Epsilon EVL constraints plus the Zeta
Java validation rules) and serialises it via `KeycloakConfigurationExporter`
into the realm-import JSON that a Keycloak instance consumes at boot —
string/file/stream, keys emitted in `TreeMap` order and null-valued properties
omitted, so the export is deterministic and diffable. `KeycloakObjectMapper`
closes the loop in the other direction, deserialising that JSON back into EMF
`EList` collections through MixIn classes.

What it ships: the EMF-generated Java API (interfaces, `*Impl`, factory,
package, builders — regenerated from `keycloak.genmodel` by an MWE2 workflow),
the hand-written runtime (`KeycloakUtils` model loading/query, the exporter,
the Jackson mapper, `KeycloakEpsilonValidator`), and three delivery shapes of
the same artifact: an Eclipse plugin, an installable Eclipse feature + P2
update site, and a standalone OSGi bundle for Karaf that self-registers models
found via the `Keycloak-Models` manifest header.

**Repository:** BlackBeltTechnology/judo-meta-keycloak ·
**Artifact:** `hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.keycloak`
(packaging `pom`, version `${revision}` = `1.0.1-SNAPSHOT`) ·
**License:** EPL-2.0 · **Java:** 21 · **Build:** Maven 3.8.7+ / Eclipse Tycho 4.0.13.

## Reactor map

The root `pom.xml` declares its `<modules>` inside the `modules` profile, which
is active unless `-DskipModules=true` is passed. Build order is the declared
order — `model` produces the artifact every other module consumes.

<modules>
  <module>model</module>
  <module>model-test</module>
  <module>osgi</module>
  <module>osgi-itest</module>
  <module>feature</module>
  <module>site</module>
</modules>

| Module | Artifact / packaging | What it contributes |
|---|---|---|
| `model` | `…keycloak.model`, `eclipse-plugin` | The metamodel itself and everything derived from it: `model/keycloak.ecore` + `.genmodel` + `.aird`, the MWE2 workflow that regenerates `src-gen/`, the EVL validation scripts, and the hand-written runtime (`KeycloakUtils`, `KeycloakConfigurationExporter`, `KeycloakObjectMapper`, `KeycloakEpsilonValidator`, Zeta validation rules). Every other reactor module is a consumer or a repackaging of this one. |
| `model-test` | `…keycloak.model.test`, `jar` | JUnit 5 proof that the runtime behaves: model load/query through `KeycloakUtils`, exporter JSON shape, EVL validation outcomes, and execution-context wiring. Depends on `model` at `test` scope, so it exercises the published API surface rather than internals. |
| `osgi` | `…keycloak.osgi`, `bundle` (Felix) | Repackages `model` for non-Eclipse OSGi containers. The bundle plugin embeds `../model/model` as `meta/keycloak` and `../model/src/main/epsilon/validations` as `validations`, exports `hu.blackbelt.judo.meta.keycloak*`, marks the Epsilon runtime imports `resolution:=optional`, and ships `KeycloakModelBundleTracker` for manifest-driven model discovery. |
| `osgi-itest` | `…keycloak.osgi.itest`, `jar` | Pax Exam integration tests that boot an Apache Karaf container and assert the bundle really resolves there: model loading, OSGi service registration, and validation in a live container — the check the unit tests structurally cannot make. |
| `feature` | `…keycloak.feature`, `eclipse-feature` | The installable Eclipse feature grouping the plugin, so the metamodel can be consumed from an IDE workspace. Pure descriptor module — no sources. |
| `site` | `…keycloak.site`, `eclipse-repository` | Builds the P2 update site published for "Install New Software" / consumed as a P2 repository by downstream Tycho builds. Its category P2 URLs carry hardcoded versions (see `update-category-versions`). |

## Build commands

Use the bundled Maven wrapper (`./mvnw`), never a repo-root `mvn`.

```bash
# Full build (code generation + compile + test)
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Run a single test class
./mvnw test -pl model-test -Dtest=KeycloakUtilsTest

# Full verification with OSGi integration tests
./mvnw clean verify

# Update target definition P2 URLs after dependency version changes
./mvnw clean install -P update-target-versions -f targetdefinition/pom.xml

# Update site category P2 URLs
./mvnw clean install -P update-category-versions -f site/pom.xml
```

To skip compiling tests entirely use `-Dmaven.test.skip`; to let a whole reactor
run and report every failure at the end use `-fae`.

**Code generation in Eclipse:** right-click `model/src/workflow/generateModel.mwe2`
→ Run As → MWE2 Workflow.

### Maven profiles

| Profile | Purpose |
|---|---|
| `modules` | Active by default (`skipModules != true`). Includes all six submodules. Disable with `-DskipModules=true`. |
| `sign-artifacts` | GPG-signs artifacts for release via `sign-maven-plugin`. Requires a GPG key in `settings.xml`. |
| `release-dummy` | Test deployment to `file:///tmp/...`. |
| `release-judong` | Deploy to the Judong Nexus repository (`https://nexus.judo.technology/repository/maven-judong-snapshots/`). |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH (`nexus-staging-maven-plugin`, `autoReleaseAfterClose`). |
| `update-target-versions` | Update Eclipse target definition P2 repository URLs. |
| `update-category-versions` | Update site category P2 repository URLs. |
| `generate-github-asciidoc-diagrams` | Generate PlantUML diagrams from the AsciiDoc under `.github`. |
| `update-source-code-license` | Update EPL-2.0 license headers on source files (`license-maven-plugin`, excludes `**/*.json`). |

## Technology stack

- **Eclipse EMF** 2.38.0 (`org.eclipse.emf.common` 2.41.0) — Ecore metamodel framework
- **Eclipse Tycho** 4.0.13 — Eclipse plugin / feature / P2 site builds
- **Epsilon Runtime** 2.8.0 — EVL constraint checking (separate Maven and Eclipse P2 coordinates)
- **Jackson** 2.17.2 — JSON serialisation with custom EList support
- **OSGi Core / Cmpn / Annotation** 7.0.0 — service component model
- **Apache Karaf** 4.4.7 — OSGi container for runtime deployment and Pax Exam
- **Lombok** 1.18.34 — annotation processing (non-Tycho modules only)
- **JUnit 5 (Jupiter)** 5.9.1, **Pax Exam** 4.13.5, **JaCoCo** 0.8.12, **SonarQube** (sonar-maven-plugin 3.9.1.2184)
- **Flatten Maven Plugin** 1.3.0 — resolves CI-friendly `${revision}` into the deployed POM
- **Judo Zeta** validation framework (`annotations`, `common`, `validation-core`)

## Architecture pointers

- `model/model/keycloak.ecore` — the metamodel: `Realm`, `Client`, `User`, `UserCredential`, `AttributeBinding`, each carrying GenModel `documentation` annotations.
- `model/model/keycloak.genmodel` — controls EMF Java code generation parameters.
- `model/src/workflow/generateModel.mwe2` — MWE2 workflow orchestrating `EcoreGenerator`, `HelperGenerator`, `BuilderGenerator`, `RuntimeModelGenerator`.
- `model/src/main/epsilon/validations/keycloak.evl` — EVL constraints for model integrity.
- `model/META-INF/MANIFEST.MF` — Eclipse plugin manifest: exported packages and dependencies.
- `osgi/pom.xml` (`maven-bundle-plugin` `<instructions>`) — OSGi bundle manifest generation.
- The root `pom.xml` is the parent POM: dependency management, plugin management, and every profile listed above.
- Test logging is driven by the repo-root `logback-test.xml`, wired through the `logback-test-config` property into `maven-surefire-plugin`.
- [README.md](README.md) — project overview with architecture diagrams.
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD workflow and branching strategy.

## Development environment

**Required:** Java 21 JDK · Maven 3.8.7+ (or `./mvnw`) · BlackBelt Nexus
credentials in Maven `settings.xml` (server id `blackbelt-nexus-mirror`) —
private BlackBelt artifacts do not resolve without them.

**For Eclipse IDE development:** m2e, Epsilon, XText + MWE2, Eclipse Modeling Tools.

## Git workflow

- **Main branch:** `develop` · **Release branch:** `master` (latest stable release)
- **Versioning:** `1.0.1-SNAPSHOT` (Maven) / `1.0.1.qualifier` (Eclipse)
- **Feature branches:** `feature/JNG-NUMBER_summary` from `develop`
- **Bugfix branches:** `bugfix/JNG-NUMBER_summary` from release branches
- **Hotfix branches:** `hotfix/JNG-NUMBER_summary` from `master`
- **Rule:** every commit must reference a Jira ticket (`JNG-xxx`)
- CI builds append branch metadata to versions (e.g. `1.0.1.develop_40`)

## Scope guard — invariants an edit must not break

1. **Never edit `src-gen/`** — regenerated by the MWE2 workflow on every build. Hand-written code belongs in `src/main/java/`.
2. **Tycho surefire `argLine` must stay on one line** — the `<argLine>` element in the root `pom.xml` `tycho-surefire-plugin` config breaks if auto-formatted across multiple lines.
3. **Lombok cannot be used in Eclipse plugin modules** — Tycho does not support Lombok annotation processing. Lombok is confined to standard JAR modules (tests).
4. **Version duality is tooling-owned** — Maven uses `-SNAPSHOT`, Eclipse uses `.qualifier`. Flatten Maven Plugin and Tycho Versions Plugin convert automatically; never hand-sync them.
5. **P2 repository URLs contain hardcoded versions** — Tycho loads repository definitions before Maven property resolution. After changing a dependency version, run the `update-target-versions` and `update-category-versions` profiles.
6. **Nexus credentials required** — see Development environment.
7. **Jackson EList deserialization** — `KeycloakObjectMapper` uses MixIn classes extending the `*Impl` classes with collection setters; that is what lets Jackson deserialize JSON arrays into EMF `EList` collections. Removing a MixIn silently breaks import.
8. **OSGi model discovery** — bundles carrying a `Keycloak-Models` manifest header are discovered by `KeycloakModelBundleTracker` and registered as OSGi services. The header name is a contract with downstream bundles.

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.
