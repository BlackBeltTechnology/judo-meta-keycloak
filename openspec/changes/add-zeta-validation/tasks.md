# Tasks

## Implementation Order

### Phase 1: Infrastructure Setup

1. [ ] Add `judo-zeta-version` property to parent pom.xml with value `1.0.0-SNAPSHOT`
2. [ ] Add Zeta framework dependencies to model/pom.xml:
   - `hu.blackbelt.judo.zeta:validation-core`
   - `hu.blackbelt.judo.zeta:zeta-annotations`
3. [ ] Add Zeta framework dependencies to model-test/pom.xml
4. [ ] Create validation package structure:
   - `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/`
   - `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/rules/`
   - `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/extensions/`
5. [ ] Create `KeycloakValidationConstants.java` with string constants for all constraint names

### Phase 2: Core Validation Classes

6. [ ] Create `KeycloakValidator.java` entry point (following ESM's `EsmValidator.java` pattern)
7. [ ] Analyze `keycloak.evl` and `keycloak-plugin-validation.evl` to inventory all constraints
8. [ ] Create `KeycloakValidations.java` with all validation rules:
   - One `@ValidationContext` per target EClass
   - Use `@Constraint` for errors
   - Use `@Critique` for warnings
   - Use `@Guard` for conditional checks
   - Use `@Satisfies` for dependencies
9. [ ] Create any necessary extension method classes in `extensions/`
10. [ ] Ensure all validation rules use constants from `KeycloakValidationConstants`

### Phase 3: Test Infrastructure

11. [ ] Create `ValidatorType.java` enum in model-test
12. [ ] Create `AbstractKeycloakValidationTest.java` base class with:
    - `initModel()` method
    - `runValidation()` method handling both EVL and Java
    - `runEvlValidation()` private helper
13. [ ] Modify `KeycloakValidationTest.java`:
    - Extend `AbstractKeycloakValidationTest`
    - Convert existing tests to parametrized tests with `@EnumSource(ValidatorType.class)`
    - Keep all existing test models and assertions
14. [ ] Add test cases for each validation rule to ensure EVL/Java parity

### Phase 4: Performance Testing

15. [ ] Create `KeycloakValidationPerformanceTest.java`:
    - Generate synthetic model with 10,000+ elements
    - Benchmark EVL vs Java (sequential) vs Java (parallel)
    - Report timing statistics
16. [ ] Add model generation utilities for creating large test models

### Phase 5: Documentation Updates

17. [ ] Create `docs/` directory structure
18. [ ] Create `docs/validation/README.md` - validation overview
19. [ ] Create `docs/validation/java-validation-framework.md` - Zeta integration docs
20. [ ] Document all implemented validation rules with examples
21. [ ] Add references to Zeta framework documentation (do not copy content)
22. [ ] Convert `README.adoc` to `README.md`:
    - Update all sections
    - Add validation section
    - Add Zeta references
23. [ ] Convert any PlantUML diagrams in docs to Mermaid format
24. [ ] Skip conversion of files under `pages/` directory

### Phase 6: Final Validation

25. [ ] Run full test suite with both validators
26. [ ] Verify EVL and Java produce identical results for all test cases
27. [ ] Run performance benchmarks and document results
28. [ ] Update AGENTS.md with validation framework information
29. [ ] Build entire project with `mvn clean install`

## Validation Criteria

### Correctness
- [ ] All existing tests pass with both EVL and JAVA validators
- [ ] No unexpected errors or warnings
- [ ] Constraint names match exactly between EVL and Java

### Performance
- [ ] Java validation faster than EVL on 10,000+ element model
- [ ] Parallel execution provides speedup for large models

### Documentation
- [ ] All validation rules documented
- [ ] README.md complete and accurate
- [ ] Zeta framework properly referenced
- [ ] No PlantUML diagrams remaining (converted to Mermaid)

### Code Quality
- [ ] All constraint names use constants (no inline strings)
- [ ] Code follows project conventions
- [ ] No new warnings introduced
- [ ] Build succeeds without errors

## Dependencies

### Blocking
- Zeta framework must be available in local Maven repository or Nexus

### Parallelizable
- Tasks 17-24 (documentation) can run parallel to Phase 3 (testing)
- Tasks 5-10 (validation rules) can be done incrementally

## Notes

### EVL Rule Analysis
The existing EVL files appear minimal - `keycloak.evl` is empty and `keycloak-plugin-validation.evl` only imports and sets up context. If no actual constraints exist, tasks 8-9 may be simpler than expected. Need to verify actual constraint count during implementation.

### Constant Naming Convention
Follow pattern: `CONTEXT_CONSTRAINT_NAME`
Example: `REALM_NAME_NOT_EMPTY`, `CLIENT_ID_REQUIRED`
