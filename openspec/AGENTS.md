# OpenSpec Agent Instructions

## Overview

OpenSpec is a spec-driven development workflow for managing changes through structured proposals. This document guides AI assistants on how to work with OpenSpec in this project.

## Directory Structure

```
openspec/
├── AGENTS.md           # This file - AI assistant instructions
├── project.md          # Project context and conventions
├── specs/              # Active specifications
│   └── <capability>/
│       └── spec.md     # Capability specification
└── changes/            # Change proposals
    ├── <change-id>/
    │   ├── proposal.md # Change proposal
    │   ├── design.md   # Technical design (optional)
    │   ├── tasks.md    # Implementation tasks
    │   └── specs/      # Spec deltas for this change
    │       └── <capability>/
    │           └── spec.md
    └── archive/        # Completed changes
```

## Key Commands

```bash
# List all changes
openspec list

# List all specs
openspec list --specs

# Show a specific change
openspec show <change-id>

# Validate a change
openspec validate <change-id> --strict

# Apply an approved change
openspec apply <change-id>

# Archive a deployed change
openspec archive <change-id>
```

## Creating a Proposal

### 1. Choose a Change ID

Use verb-led naming: `add-*`, `remove-*`, `update-*`, `fix-*`, `refactor-*`

Examples:
- `add-zeta-validation` - Adding a new feature
- `update-validation-tests` - Modifying existing behavior
- `fix-constraint-ordering` - Bug fixes
- `refactor-test-infrastructure` - Code improvements

### 2. Create the Proposal Structure

```bash
mkdir -p openspec/changes/<change-id>/specs
```

### 3. Write proposal.md

```markdown
# <Title>

## Summary
<1-2 sentence description of what this change accomplishes>

## Motivation
<Why is this change needed? What problem does it solve?>

## Approach
<High-level description of how the change will be implemented>

## Scope
<What's included and what's explicitly excluded>

## Risks
<Potential issues and mitigation strategies>

## Related
- <Links to related changes, specs, or external docs>
```

### 4. Write tasks.md

```markdown
# Tasks

## Implementation Order

1. [ ] <First task - must be specific and verifiable>
2. [ ] <Second task>
3. [ ] <Third task>

## Validation

- [ ] All tests pass
- [ ] Documentation updated
- [ ] Build succeeds
```

### 5. Write design.md (when needed)

Required when:
- Change spans multiple systems
- Introduces new patterns
- Requires trade-off discussion
- Has multiple valid approaches

```markdown
# Design: <Title>

## Context
<Background and current state>

## Decision
<Chosen approach and rationale>

## Alternatives Considered
<Other approaches and why they weren't chosen>

## Consequences
<Positive and negative outcomes>
```

### 6. Write Spec Deltas

For each affected capability, create `specs/<capability>/spec.md`:

```markdown
# <Capability Name>

## ADDED Requirements

### Requirement: <Name>
<Description of new requirement>

#### Scenario: <Name>
Given <context>
When <action>
Then <expected outcome>

## MODIFIED Requirements

### Requirement: <Name>
<Updated description>

## REMOVED Requirements

### Requirement: <Name>
<Reason for removal>
```

## Workflow Stages

### 1. Proposal Stage
- Create proposal, tasks, design (optional), and spec deltas
- Run `openspec validate <change-id> --strict`
- Request user approval

### 2. Apply Stage
- After approval, run `openspec apply <change-id>`
- Implement tasks in order
- Mark tasks complete as you go

### 3. Archive Stage
- After deployment, run `openspec archive <change-id>`
- Moves change to `changes/archive/`

## Best Practices

### DO
- Keep proposals focused on a single cohesive change
- Write specific, verifiable tasks
- Include validation criteria
- Document design decisions
- Cross-reference related changes

### DON'T
- Write code during proposal stage
- Combine unrelated changes
- Skip validation step
- Leave tasks vague or ambiguous
- Forget to update specs when behavior changes

## Project-Specific Notes

### Keycloak Meta Specifics
- EVL validation rules in `model/src/main/epsilon/validations/`
- Java validation to be added in `model/src/main/java/hu/blackbelt/judo/meta/keycloak/validation/`
- Tests in `model-test/src/test/java/`
- Use Zeta framework from `hu.blackbelt.judo.zeta`

### Validation Framework Migration
When migrating from EVL to Zeta:
1. Use `@ValidationContext` for target EClass
2. Use `@Constraint` for errors, `@Critique` for warnings
3. Use `@Satisfies` for constraint dependencies
4. Use `@Guard` for conditional execution
5. Keep EVL and Java running in parallel with parametrized tests
