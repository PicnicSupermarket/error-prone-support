---
applyTo: "**/.github/instructions/**"
---

# Instruction File Conventions

This document describes how `.github/instructions/*.instructions.md` files are
written and maintained in this project.

All instruction files must also follow the writing conventions in
[`documentation.instructions.md`][documentation].

## Instruction files serve all agents and humans

Instruction files are the canonical reference for coding conventions in this
project. They are designed to be consulted by:

- AI coding agents of any platform (Claude, Copilot, Gemini, Codex and others).
- AI models of any capability level (from small/fast models to large frontier
  models).
- Human contributors.

Write rules clearly and unambiguously. Use concrete **Do**/**Don't** examples.
Avoid jargon that assumes familiarity with a specific agent platform.

## `applyTo` frontmatter
<!-- check: `applyTo` frontmatter is present and targets the right files -->

Every instruction file must include an `applyTo` field in the YAML frontmatter,
with the following exceptions (which apply to workflow actions rather than
specific source files and are loaded via skills, not pattern matching):

- `review.instructions.md` (auto-generated, loaded explicitly during review).
- `commit-message.instructions.md` (applies when writing commit messages).
- `pull-request.instructions.md` (applies when creating pull requests).

The `applyTo` field tells agent platforms (e.g., GitHub Copilot) which files
the instructions apply to. Use glob patterns. These globs are best-effort
hints: they may be approximate when a file's scope spans multiple file types or
directories.

```yaml
---
applyTo: "**/*.java"
---
```

## Review checklist annotations

Instruction files support `<!-- check: -->` HTML comment annotations that feed
into the auto-generated review checklist (`review.instructions.md`). The
annotations are invisible in rendered markdown but are parsed by
`./generate-review-checklist.sh`.

### Annotation syntax
<!-- check: skip -->

Annotations are placed on the line immediately after a heading.

**Default** (no annotation): the heading text becomes a checklist item.

```markdown
### Prefer Guava immutable collections
```

Produces: `- [ ] Prefer Guava immutable collections`

**Custom text**: override the default with a more specific item.

```markdown
### Prefer Guava immutable collections
<!-- check: All collection types use `ImmutableList`/`ImmutableSet`/`ImmutableMap` -->
```

Produces: `- [ ] All collection types use ...`

**Multiple items**: one heading can produce multiple checklist items.

```markdown
### Declare least-privilege permissions
<!-- check: `permissions` declared at workflow level with `contents: read` -->
<!-- check: Job-level permissions scoped to only what is needed -->
```

**Skip**: exclude a heading from the checklist.

```markdown
### Overview
<!-- check: skip -->
```

### Heading levels in the generated checklist
<!-- check: skip -->

- `##` headings become section group labels (not checklist items) unless
  annotated with `<!-- check: -->`.
- `###` and deeper headings become checklist items by default.

## Regenerate the review checklist after editing
<!-- check: `./generate-review-checklist.sh` executed after instruction file changes -->

After editing any instruction file, run:

```sh
./generate-review-checklist.sh
```

This regenerates `.github/instructions/review.instructions.md`. Commit the
updated file alongside your instruction file changes. CI validates that the
generated file is in sync with the source files.

## Section headers must be review-compatible
<!-- check: Every section header has an explicit `check:` annotation, or is self-evidently verifiable -->
<!-- check: `check: skip` is used only for non-verifiable sections (context, background, parent groups) -->

Every section header in an instruction file must be unambiguous to the
review-checklist generator and to AI agents reading the file for review
guidance:

- If the heading text reads naturally as a checklist item (i.e., "Verify:
  [heading text]" makes sense), no annotation is needed.
- Otherwise, add an explicit `<!-- check: TEXT -->` annotation (or multiple;
  see the next section) immediately after the heading.
- Use `<!-- check: skip -->` only for sections that describe context,
  background, parent groupings, or other non-verifiable information.

A heading that is neither self-evidently verifiable nor explicitly annotated is
a defect: less capable agents will misinterpret it.

## Bias towards multiple check annotations
<!-- check: Sections with multiple verifiable requirements list multiple `check:` annotations -->

When a section covers more than one verifiable requirement, emit one `<!--
check: -->` annotation per requirement rather than a single coarse item.

**Do:**

```markdown
### Declare least-privilege permissions
<!-- check: `permissions` declared at workflow level with `contents: read` -->
<!-- check: Job-level permissions scoped to only what is needed -->
```

**Don't:**

```markdown
### Declare least-privilege permissions
<!-- check: Permissions are declared correctly -->
```

Multiple fine-grained items make agent execution more predictable: agents can
process each item independently and less capable models are less likely to
conflate unrelated requirements into a single pass.

## Add `<!-- check: -->` annotations to every new section
<!-- check: New sections carry a `<!-- check: -->` annotation, or the heading is self-evidently verifiable -->

Whenever you add a new section to an instruction file, annotate it
immediately. An unannotated heading is a silent defect: for `##` headings the
item is silently dropped from the checklist; for `###` and deeper the raw
heading text is used, which may be poorly worded or ambiguous. The only
acceptable reasons to omit a `<!-- check: -->` annotation are:

- The heading text is self-evidently verifiable as a checklist item ("Verify:
  [heading text]" makes immediate sense without additional context).
- The section contains only context or background, in which case use
  `<!-- check: skip -->`.

## `review.instructions.md` is auto-generated

Do not edit `review.instructions.md` manually. It is derived from the `<!--
check: -->` annotations in the other instruction files. Any manual edits will
be overwritten by the generator and rejected by CI.

[documentation]: documentation.instructions.md
