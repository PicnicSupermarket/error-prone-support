---
applyTo: "**/.github/workflows/**"
---

# GitHub Actions Workflow Conventions

This document describes the conventions for creating and modifying GitHub
Actions workflows in this project. It serves as the canonical reference for
both AI coding agents and human contributors.

## Pin all actions by commit hash
<!-- check: All actions pinned by full SHA with version comment -->

All external actions must be pinned by full commit SHA. Always include a
version comment after the hash.

**Do:**

```yaml
- uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2
```

**Don't:**

```yaml
- uses: actions/checkout@v4
- uses: actions/checkout@main
```

## Use consistent action versions
<!-- check: Newly added actions use the same hash/version as existing usages in the repo -->

When adding a step that uses an action already present in another workflow, use
the same commit hash and version comment. Do not upgrade an action as a side
effect of an unrelated change.

When adding an action that is not yet used anywhere in this repository, pin it
to its most recent release.

> Note: the example hashes throughout this file may be outdated. Always check
> the existing workflow files for the current hash and version comment used for
> each action.

## Include `step-security/harden-runner` in every job
<!-- check: `step-security/harden-runner` is the first step of every job -->
<!-- check: `disable-sudo-and-containers: true` (or `disable-sudo: true` if incompatible) -->
<!-- check: `egress-policy` is omitted (defaults to `block`; use `audit` only while developing) -->

The first step of every job must be `step-security/harden-runner`.

- Set `disable-sudo-and-containers: true` by default. Use `disable-sudo: true`
  only when the workflow requires containers (document the reason with a
  comment).
- Do not set `egress-policy`; it defaults to `block`. Set `egress-policy:
  audit` only temporarily while developing a new workflow to discover required
  endpoints; remove it before merging.
- Configure `allowed-endpoints` explicitly.

```yaml
steps:
  - name: Install Harden-Runner
    uses: step-security/harden-runner@fa2e9d605c4eeb9fcad4c99c224cee0c6c7f3594 # v2.16.0
    with:
      disable-sudo-and-containers: true
      allowed-endpoints: >
        github.com:443
        api.github.com:443
```

## Declare least-privilege permissions
<!-- check: `permissions` declared at workflow level with `contents: read` -->
<!-- check: Job-level permissions scoped to only what is needed -->

Always declare `permissions` at workflow level with `contents: read`. Add
job-level permissions only for what each job needs. Never use `permissions:
write-all`.

**Do:**

```yaml
permissions:
  contents: read
jobs:
  deploy:
    permissions:
      contents: read
      pages: write
```

**Don't:**

```yaml
permissions: write-all
```

## Use explicit runner versions
<!-- check: Runner uses explicit version (not `ubuntu-latest`) -->

Use explicit runner versions, not the `-latest` alias. The `-latest` alias can
change between workflow runs, breaking reproducibility.

- Linux: `ubuntu-24.04`
- macOS: `macos-15`
- Windows: `windows-2025`

## Every step must have a name
<!-- check: All steps have a `name:` field -->

All steps must include a `name:` field. Use imperative form for step names.

**Do:**

```yaml
- name: Check out code
  uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2
- name: Build project
  run: mvn clean verify
```

**Don't:**

```yaml
- uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2
- run: mvn clean verify
```

## Use explicit `uses:` key
<!-- check: Steps use `- uses:` syntax (not shorthand) -->

Always write `- uses: action@hash`. Do not use the shorthand form `-
action@hash`.

## Wrap `if:` expressions in `${{ }}`
<!-- check: All `if:` conditions use `${{ }}` wrapping -->

All `if:` conditions must be wrapped in `${{ }}`.

**Do:**

```yaml
if: ${{ github.ref == github.event.repository.default_branch }}
```

**Don't:**

```yaml
if: github.ref == github.event.repository.default_branch
```

## Never interpolate `${{ }}` expressions into `run:` or `script:` blocks
<!-- check: No `${{ }}` expressions are interpolated into `run:` or `script:` blocks -->

Never embed `${{ expression }}` directly inside a `run:` shell block or a
`github-script` `script:` block. GitHub Actions evaluates the expression before
passing the result to the shell or JavaScript engine. If the expression
contains user-controlled content (e.g. a PR title, comment body, or branch name
from a fork), the substituted value can break out of its string context and
execute arbitrary code.

Pass the value through an environment variable instead; the shell and the
JavaScript runtime then treat it as data, not code.

**Do:**

```yaml
- name: Use value
  run: echo "${MY_VALUE}"
  env:
    MY_VALUE: ${{ github.event.comment.body }}
```

**Don't:**

```yaml
- name: Use value
  run: echo "${{ github.event.comment.body }}"
```

The same rule applies to `github-script` `script:` blocks: pass expressions
through `env:` and read them via `process.env.MY_VAR`.

Exception: runner-owned context values such as `runner.temp` and `runner.os`
cannot contain user-supplied content, so interpolating them directly is safe.

## Omit action inputs that match their default value
<!-- check: Action inputs that match their defaults are omitted -->

Do not specify action `with:` inputs when they match the action's default
value. This reduces noise and makes intentional overrides obvious.

## Naming conventions
<!-- check: Workflow names use imperative form (e.g., "Run mutation tests") -->
<!-- check: Job names are short, lowercase identifiers (e.g., `build`, `validate`) -->
<!-- check: Step names use imperative form (e.g., "Install Harden-Runner") -->

- **Workflow names**: use imperative form ("Run mutation tests", "Validate
  review checklist"). Avoid bare noun phrases ("Mutation testing").
- **Job names**: short, lowercase identifiers (`build`, `validate`, `deploy`,
  `analyze`).
- **Step names**: use imperative form ("Install Harden-Runner", "Check out
  code", "Build project").

## Java and Maven setup

For workflows that build Java code:

- Use the `s4u/setup-maven-action` for checkout, JDK, and Maven setup in a
  single step.
- Set `MAVEN_ARGS` as an environment variable at the workflow or job level:
  ```yaml
  env:
    MAVEN_ARGS: --batch-mode -Dstyle.color=always -T1C
  ```
- Use the current standard JDK and Maven versions (see existing workflows for
  the latest values).

## Configure workflow triggers

- Use `paths:` trigger filters when a workflow only applies to specific files.

## YAML formatting

- 2-space indentation.
- Single quotes for simple strings.
- Comments use `#` with a leading space.

### Use bracket notation for short YAML arrays
<!-- check: Bracket arrays use whitespace padding (`[ a, b ]`) -->

Use flow-style bracket notation with whitespace padding for short arrays
(branches, types, needs, matrix values). Use block-style list notation (`-
item`) for longer arrays or multi-line values.

**Do:**

```yaml
branches: [ master ]
types: [ created ]
needs: [ build, test ]
os: [ ubuntu-24.04, macos-15, windows-2025 ]
```

**Don't:**

```yaml
branches: [master]
branches:
  - master
```
