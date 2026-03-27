# Commit Message Conventions

This document describes the conventions for writing commit messages in this
repository. These conventions apply to all commits, whether made manually or by
AI coding agents.

## Subject line format
<!-- check: Subject line uses imperative mood, starts capitalized, no trailing period -->

The subject line is the first line of the commit message. Format it as follows:

- Use imperative mood: "Introduce", "Fix", "Extend", not "Introduces", "Fixed",
  "Extending".
- Start with a capital letter.
- Do not end with a period.
- Try not to exceed 80 characters, but go over if needed for clarity.
- Wrap code references (class names, method names, file names) in backticks.
- Use the curly-brace shorthand for related items (e.g., "Introduce
  `{Foo,Bar}Baz` Refaster rules").

## Subject line verbs
<!-- check: New Refaster rule subjects follow "Introduce `{RuleName}` Refaster rule" pattern -->
<!-- check: New Refaster collection subjects follow "Introduce `{Topic}Rules` Refaster rule collection" pattern -->
<!-- check: Refaster rule extension subjects follow "Extend `{RuleName}` Refaster rule" pattern -->
<!-- check: New BugChecker subjects follow "Introduce `{CheckerName}` check" pattern -->
<!-- check: Upgrade subjects follow "Upgrade {Name} {old} -> {new}" pattern -->

To understand the appropriate verb and phrasing for a given change, inspect the
100 most recent non-merge commits:

```sh
git log --format='%s' --no-merges -n 100
```

The following special formats must be followed exactly:

- When adding new Refaster rules to an existing collection: "Introduce
  `{RuleName}` Refaster rule[s]".
- When adding a new Refaster rule collection: "Introduce `{Topic}Rules`
  Refaster rule collection".
- When extending Refaster rules: "Extend `{RuleName}` Refaster rule[s]".
- When adding a new `BugChecker`: "Introduce `{CheckerName}` check".
- When upgrading a dependency: "Upgrade {Name} {old} -> {new}" (see dedicated
  section below).

## PR number suffix
<!-- check: skip -->

After a pull request is created, the subject line ends with ` (#N)` where `N`
is the PR number. This suffix is added after the PR is created (see
[`pull-request.instructions.md`](pull-request.instructions.md)). It does not
apply to local-only commits.

## Commit body
<!-- check: skip -->

When a commit warrants explanation beyond the subject line:

- Wrap body paragraphs at 72 characters.
- Focus on "what" and "why", not implementation details.
- Use `While there,` to describe bundled incidental changes.
- If multi disparate things are changed, use `Summary of changes:` followed by
  a bulleted list for complex multi-part commits.
- Use `Resolves #N.` (with period) to close GitHub issues.
- List URLs last, preceded by `See:` if there is one, and `See:\n` followed by
  a list of `- URL` items if there are multiple.
- Never split URLs across lines, even if they exceed 72 characters.


## Upgrade commits
<!-- check: skip -->

Dependency upgrade commits use the subject format:

```
Upgrade {Name} {old-version} -> {new-version}
```

Rules:

- Use the dependency's display name as established in past commits (e.g.,
  "Spring Boot" not "spring-boot", "Error Prone" not "error-prone").
- For GitHub Actions: `Upgrade {owner}/{action} v{old} -> v{new}`.
- For JDK upgrades: `Upgrade JDKs used by GitHub Actions builds`.
- Generally, each dependency is upgraded in its own pull request.

## Upgrade commit body: release notes URLs
<!-- check: Upgrade commits include release notes URLs matching past commit patterns -->

Upgrade commits must include a `See:` section listing release notes URLs.
Order the entries as follows:

1. Custom release note documents (wiki, docs, JIRA, changelog).
2. GitHub release pages for all intermediate versions, in ascending order.
3. The full compare URL.

Include ALL intermediate versions. When upgrading from v1.0 to v1.3, include
release links for v1.1, v1.2, and v1.3.

To discover the correct URL patterns for a given dependency, check its most
recent upgrade commits:

```sh
git log --grep='^Upgrade Spr' --format='%B%n---' -n 10
```

If no past commits exist, the default GitHub URL pattern is:

- Release: `https://github.com/{owner}/{repo}/releases/tag/v{new}`
- Compare: `https://github.com/{owner}/{repo}/compare/v{old}...v{new}`

If relevant URLs cannot be found, a commit message that consists only of the
summary line is acceptable.

Do not hallucinate URLs. Do not fabricate version numbers.

## Examples
<!-- check: skip -->

**Simple Refaster rule introduction (subject only):**

```
Introduce `{Least,Greatest}{,NaturalOrder}` Refaster rules (#2133)
```

**BugChecker introduction (subject and body):**

```
Introduce `LexicographicalPermitsListing` check (#2017)

As this new check is structurally similar to
`LexicographicalAnnotationListing`, the shared logic is factored out
and exposed through `SourceCode#sortTrees`.

While there, optimize both checks for the common case in which no
violation is found, by reducing the number of allocations for
compliant code.
```

**Upgrade with standard GitHub URLs:**

```
Upgrade Mockito 5.22.0 -> 5.23.0 (#2157)

See:
- https://github.com/mockito/mockito/releases/tag/v5.23.0
- https://github.com/mockito/mockito/compare/v5.22.0...v5.23.0
```

**Upgrade across multiple intermediate versions:**

```
Upgrade CodeQL v4.31.11 -> v4.32.4 (#2117)

See:
- https://github.com/github/codeql-action/blob/main/CHANGELOG.md
- https://github.com/github/codeql-action/releases/tag/v4.32.0
- https://github.com/github/codeql-action/releases/tag/v4.32.1
- https://github.com/github/codeql-action/releases/tag/v4.32.2
- https://github.com/github/codeql-action/releases/tag/v4.32.3
- https://github.com/github/codeql-action/releases/tag/v4.32.4
- https://github.com/github/codeql-action/compare/v4.31.11...v4.32.4
```

**Upgrade with custom URLs and bundled changes:**

```
Upgrade Checkstyle 13.2.0 -> 13.3.0 (#2128)

While there, enable the new `GoogleNonConstantFieldName` and
`UseEnhancedSwitch` checks.

See:
- https://checkstyle.sourceforge.io/releasenotes.html
- https://github.com/checkstyle/checkstyle/releases/tag/checkstyle-13.3.0
- https://github.com/checkstyle/checkstyle/compare/checkstyle-13.2.0...checkstyle-13.3.0
```

**Upgrade with Picnic Error Prone fork URLs:**

```
Upgrade Error Prone 2.47.0 -> 2.48.0 (#2129)

See:
- https://github.com/google/error-prone/releases/tag/v2.48.0
- https://github.com/google/error-prone/compare/v2.47.0...v2.48.0
- https://github.com/PicnicSupermarket/error-prone/compare/v2.47.0-picnic-1...v2.48.0-picnic-1
```
