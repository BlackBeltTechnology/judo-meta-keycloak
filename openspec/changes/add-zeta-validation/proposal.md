# Add Zeta Validation Framework to Keycloak Meta

## Summary

Implement native Java-based validation for Keycloak metamodel using the Zeta validation framework, running in parallel with existing EVL validation. This mirrors the ESM module's dual-validation approach.

## Motivation

1. **Performance**: Java validation is significantly faster than EVL interpretation
2. **IDE Support**: Full IDE integration for validation rules (debugging, refactoring, code completion)
3. **Type Safety**: Compile-time checking of validation logic
4. **Consistency**: Align with ESM module's validation approach
5. **Maintainability**: Easier to understand and modify validation rules in native Java

## Approach

### Phase 1: Infrastructure Setup
- Add Zeta framework dependency to pom.xml with SNAPSHOT version
- Create validation package structure in model module
- Create `KeycloakValidator` entry point class

### Phase 2: EVL to Java Migration
- Analyze existing EVL rules in `keycloak.evl` and `keycloak-plugin-validation.evl`
- Convert each EVL constraint/critique to Java using Zeta annotations
- Use constants for all constraint names, validation result messages, guard method names
- Maintain identical validation semantics

### Phase 3: Test Infrastructure
- Create `ValidatorType` enum (EVL, JAVA)
- Create `AbstractKeycloakValidationTest` base class with parametrized test support
- Modify existing `KeycloakValidationTest` to extend base class and use parametrized tests
- Both validators run with same test cases and assertions

### Phase 4: Performance Testing
- Create `KeycloakValidationPerformanceTest`
- Generate model with 10,000+ elements programmatically
- Compare EVL vs Java (sequential) vs Java (parallel)

### Phase 5: Documentation
- Convert README.adoc to README.md
- Convert PlantUML diagrams to Mermaid
- Create `docs/validation/` documentation structure
- Reference Zeta documentation (link, don't copy)
- Document all implemented validation rules

## Scope

### Included
- Zeta validation framework integration
- All EVL rules converted to Java
- Parametrized tests running both validators
- Performance benchmarks
- Documentation updates
- Convert .adoc to .md (except pages/)
- PlantUML to Mermaid conversion

### Excluded
- Changes to Keycloak metamodel (ecore)
- Changes to code generation
- New validation rules (only convert existing)
- Changes to EVL files (kept for comparison)

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Validation result differences | High | Comprehensive parametrized tests ensure parity |
| Performance regression | Medium | Benchmarks track performance metrics |
| Dependency conflicts | Low | Zeta is standalone with minimal dependencies |
| Breaking existing tests | High | Keep existing tests, extend with parametrized approach |

## Dependencies

### External
- `hu.blackbelt.judo.zeta:validation-core:1.0.0-SNAPSHOT` - Zeta validation framework
- `hu.blackbelt.judo.zeta:zeta-annotations:1.0.0-SNAPSHOT` - Zeta annotations

### Internal
- Existing Keycloak metamodel structure
- KeycloakModel and KeycloakUtils runtime classes

## Related

- ESM validation framework: `/Users/robson/Project/judo-ng/models/judo-meta-esm/model/src/main/java/hu/blackbelt/judo/meta/esm/validation/`
- Zeta framework: https://github.com/BlackBeltTechnology/judo-zeta
- EVL rules: `model/src/main/epsilon/validations/keycloak*.evl`
