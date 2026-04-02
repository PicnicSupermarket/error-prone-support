---
description: >
  Auto-generated review checklist derived from the project's instruction files.
  Do not edit manually; edit the source instruction files instead and then run
  `./generate-review-checklist.sh `to regenerate.
---

# Review Checklist

Before committing, or any time you are asked to review some aspect of this
project, verify each applicable item. Items are grouped by source instruction
file. Not all sections apply to every change; focus on the sections relevant to
the files you modified.

## Agent Configuration Conventions (`agents.instructions.md`)

### `AGENT.md` is the canonical entry point

- [ ] `AGENT.md` is the single source of truth for agent workflow

### Use symlinks to avoid duplication across agent platforms

- [ ] Agent-specific files use symlinks to canonical sources

## Bug Checker Conventions (`bug-checkers.instructions.md`)

### File locations

- [ ] Checker is in the correct module (contrib vs experimental vs guidelines)

### Checker file structure

- [ ] `serialVersionUID = 1L` is present
- [ ] Public no-arg constructor with Javadoc is present
- [ ] `SourceCode#treeToString` is used instead of `Tree#toString()`

### Test file structure

- [ ] `// BUG: Diagnostic contains:` is on the line before the flagged code
- [ ] Identification test includes negative (non-flagged) cases listed first
- [ ] `identification()` test cases follow checker validation logic

### Avoid suggesting breaking changes in fixes

- [ ] Suggested fixes do not introduce compilation errors

## Commit Message Conventions (`commit-message.instructions.md`)

### Subject line format

- [ ] Subject line uses imperative mood, starts capitalized, no trailing period

### Subject line verbs

- [ ] New Refaster rule subjects follow "Introduce `{RuleName}` Refaster rule" pattern
- [ ] New Refaster collection subjects follow "Introduce `{Topic}Rules` Refaster rule collection" pattern
- [ ] Refaster rule extension subjects follow "Extend `{RuleName}` Refaster rule" pattern
- [ ] New BugChecker subjects follow "Introduce `{CheckerName}` check" pattern
- [ ] Upgrade subjects follow "Upgrade {Name} {old} -> {new}" pattern

### Commit body

- [ ] Commit body does not repeat or lightly paraphrase the subject line

### Upgrade commit body: release notes URLs

- [ ] Upgrade commits include release notes URLs matching past commit patterns
- [ ] Upgrades of listed libraries include wiki or release notes page
- [ ] No URLs are hallucinated
- [ ] Release notes are not summarized in commit message

## Documentation Conventions (`documentation.instructions.md`)

### Text width

- [ ] Prose wraps at 79 characters

### Use only ASCII characters

- [ ] No non-ASCII characters (no em-dashes, special arrows, etc.)

### Punctuation in lists

- [ ] Enumerated items end with a period

### `XXX` comments for future work

- [ ] Use `XXX:` for future work (not `TODO:` or `FIXME:`)

## GitHub Actions Workflow Conventions (`github-actions.instructions.md`)

### Pin all actions by commit hash

- [ ] All actions pinned by full SHA with version comment

### Include `step-security/harden-runner` in every job

- [ ] `step-security/harden-runner` is the first step of every job
- [ ] `disable-sudo-and-containers: true` (or `disable-sudo: true` if incompatible)
- [ ] `egress-policy` is omitted (defaults to `block`; use `audit` only while developing)

### Declare least-privilege permissions

- [ ] `permissions` declared at workflow level with `contents: read`
- [ ] Job-level permissions scoped to only what is needed

### Use explicit runner versions

- [ ] Runner uses explicit version (not `ubuntu-latest`)

### Every step must have a name

- [ ] All steps have a `name:` field

### Use explicit `uses:` key

- [ ] Steps use `- uses:` syntax (not shorthand)

### Wrap `if:` expressions in `${{ }}`

- [ ] All `if:` conditions use `${{ }}` wrapping

### YAML formatting

- [ ] Bracket arrays use whitespace padding (`[ a, b ]`)

## Instruction File Conventions (`instructions.instructions.md`)

### `applyTo` frontmatter

- [ ] `applyTo` frontmatter is present and targets the right files

### Regenerate the review checklist after editing

- [ ] `./generate-review-checklist.sh` executed after instruction file changes

## Java Style Conventions (`java-style.instructions.md`)

### Collections and immutability

- [ ] Use Guava immutable collections
- [ ] Use Guava immutable collectors
- [ ] Use immutable types for parameters and return types
- [ ] Use builder pattern for conditional collection construction
- [ ] Use Guava `Multimap` instead of `Map<K, Collection<V>>`

### Nullability and optionals

- [ ] Prefer `Optional` over `@Nullable`
- [ ] Use JSpecify `@Nullable` and `@NullMarked`
- [ ] Use `requireNonNull` with a descriptive message
- [ ] Use `requireNonNullElse` instead of Guava's `firstNonNull`

### Functional style

- [ ] Favour early returns, simplest cases first
- [ ] Prefer ternary return over `if`/`return` blocks
- [ ] Prefer `Consumer<T>` sink parameters over mutable collection parameters
- [ ] Prefer streams over imperative loops
- [ ] Prefer method references over lambdas
- [ ] Do not pass `Stream` as a method parameter
- [ ] Inline single-use fields and variables

### Error Prone API usage

- [ ] Define matchers as `private static final` fields
- [ ] Prefer static `Matcher` fields over manual boolean predicate methods
- [ ] Prefer `instanceof` pattern matching over `Tree#getKind()`
- [ ] Compose matchers with `allOf()`, `anyOf()`, `not()`
- [ ] Use `ASTHelpers` qualified, not statically imported
- [ ] Use `SourceCode`, `MoreASTHelpers`, `Documentation` from `error-prone-utils`
- [ ] Use guard clauses for `Description.NO_MATCH`
- [ ] Avoid `compilesWithFix` in `BugChecker` implementations
- [ ] `VisitorState` comes last in method signatures
- [ ] `@BugPattern` summary should not end with a period
- [ ] BugChecker Javadoc: first line is a `{@link}` to the replaced/flagged element

### Imports and formatting

- [ ] No wildcard imports
- [ ] Code formatted with `mvn fmt:format`

### Javadoc and comments

- [ ] Use `{@code}` and `{@link}` in Javadoc
- [ ] Use `<p>` for paragraph breaks
- [ ] Apply `@SuppressWarnings` to the smallest possible scope
- [ ] Use `// XXX:` for future work, not `// TODO:`
- [ ] Javadoc sentences must end with a period
- [ ] Always include `@param`, `@return`, and `@throws` tags
- [ ] Private methods generally do not need Javadoc

### Class structure and naming

- [ ] Prefer `final` classes and minimal visibility
- [ ] Fields are ordered: static final, static non-final, instance final, instance non-final
- [ ] Methods are ordered: constructors, static factory methods, overrides, then by usage order: first instance, then static
- [ ] No empty first line inside a class body
- [ ] Keep lists and members sorted lexicographically
- [ ] Utility classes are `final` with a private constructor
- [ ] Class and method names follow project naming conventions
- [ ] `BugChecker` has a public no-arg constructor with Javadoc

### Java language features

- [ ] Use text blocks for multi-line strings
- [ ] Use `instanceof` pattern matching
- [ ] Use switch expressions
- [ ] Use records for immutable data carriers
- [ ] Do not use `var`

### Guava utilities

- [ ] Use `Preconditions` for validation
- [ ] Use Guava collection utilities for set algebra

## Pull Request Conventions (`pull-request.instructions.md`)

### PR title

- [ ] PR title matches the commit message summary (without `(#N)`)

### Label

- [ ] PR has exactly one label from `.github/release.yml`

### Milestone

- [ ] PR targets the nearest upcoming milestone

### Reviewers

- [ ] PR assigns the correct reviewers

### Description format

- [ ] PR description contains a suggested commit message in a fenced code block

## Refaster Rule Conventions (`refaster-rules.instructions.md`)

### Rule file structure

- [ ] Rule classes use `static final class` (not `abstract`) unless `@Placeholder` is needed
- [ ] Javadoc follows "Prefer X over Y" format
- [ ] Javadoc qualifier is correct (deprecated, less efficient, etc.)
- [ ] `@BeforeTemplate`s use already-rewritten sub-expressions
- [ ] New rule has distinct `@AfterTemplate`
- [ ] New rule does not make existing rules redundant
- [ ] Parameter names follow type-based naming conventions
- [ ] Rule class name is derived from `@AfterTemplate` identifiers
- [ ] `@SuppressWarnings` entries have explanatory comments
- [ ] Behavior-changing rules have `<p><strong>Warning:</strong>` in Javadoc
- [ ] Known limitations are documented with `// XXX:` comments
- [ ] Type parameters are as wide as possible; wildcard bounds are eliminated
- [ ] Use the most specific return type

### Advanced patterns

- [ ] Prefer `Refaster.anyOf` over multiple `@BeforeTemplate` methods

### Test input file

- [ ] Test method names match inner class names exactly (`testFooBar` for `FooBar`)
- [ ] Test class is named `{Topic}RulesTest` (not `*TestInput`/`*TestOutput`)
- [ ] `elidedTypesAndStaticImports()` lists all replaced types/imports

### Collection registration

- [ ] Collection is registered in `RefasterRulesTest.java` `RULE_COLLECTIONS`
- [ ] `RULE_COLLECTIONS` entries are in alphabetical order

## Shell Script Conventions (`scripts.instructions.md`)

### Structure

- [ ] Script starts with `#!/usr/bin/env bash` and `set -e -u -o pipefail`
- [ ] Script has a header comment explaining its purpose

### Variables

- [ ] Variables use the correct casing convention
- [ ] All variable expansions are quoted with `"${var}"`
- [ ] Command substitutions use `$(...)` syntax

### Functions

- [ ] Functions use `function name() {` syntax with `local` for all variables

### Conditionals and tests

- [ ] Conditionals use `[ ]` (not `[[ ]]`)

### Error handling

- [ ] Error and usage messages use `echo "..." >&2`

### Temporary files

- [ ] Temporary files use `mktemp -d` with a `trap` cleanup handler

### Formatting

- [ ] Two-space indentation (no tabs)

## Skill File Conventions (`skills.instructions.md`)

### Skills are small and reference instruction files

- [ ] Skill file is small (under 30 lines) and references instruction files

### Skill description determines when the skill triggers

- [ ] Skill `description` clearly states when to use the skill

## Testing Conventions (`testing.instructions.md`)

### General conventions

- [ ] Test classes are `final` and named `{ClassName}Test`
- [ ] Prefer parameterized tests over multiple similar test methods
- [ ] Keep test inputs minimal
- [ ] Use distinct values per test line
- [ ] Use metasyntactic variable names
- [ ] Format test source strings with Google Java Format

### Mutation testing

- [ ] Run mutation tests
- [ ] Kill every killable mutant
- [ ] Mutation test results are analyzed and surviving mutants are addressed
- [ ] Document unkillable mutants
