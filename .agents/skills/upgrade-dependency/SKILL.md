---
name: upgrade-dependency
description: >
  Use this skill when upgrading a Maven dependency, GitHub Action, or other
  versioned dependency in this repository.
---

# Upgrade Dependency

Use this skill when asked to upgrade a dependency version.

Read [`.github/instructions/commit-message.instructions.md`][commit-message]
for the upgrade commit conventions. Then:

1. **Update the version** in the top-level `pom.xml` (or workflow file for
   Actions). Also update any `<!-- Renovate: ... -->` markers in
   `integration-tests/*-init.patch` files, preserving the existing coordinate
   convention (which may reference a BOM artifact rather than the dependency
   itself).
2. **Run a full build** to verify the upgrade.
3. **Check past upgrade commits** for this dependency to learn the URL pattern:
   `git log --grep='^Upgrade {Name}' --format='%B%n---' -n 10`.
4. **Discover intermediate releases** between old and new versions using the
   GitHub API (see commit message conventions).
5. **Write the commit message** following upgrade conventions, including all
   release notes URLs.
6. **Commit** the result.

[commit-message]: ../../../.github/instructions/commit-message.instructions.md
