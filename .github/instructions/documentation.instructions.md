---
applyTo: "**/*.md,**/*.sh,**/*.xml,**/*.yml"
---

# Documentation Conventions

This document describes the writing conventions for documentation in this
repository. These conventions apply to all text files: Markdown, shell script
comments, YAML comments, XML comments, and any other file containing prose or
comments. For Java-specific conventions (including Javadoc), see
[`java-style.instructions.md`][java-style].

## Text width
<!-- check: Prose wraps at 79 characters -->

Wrap prose at 79 characters. In vim: `set tw=79`. This applies to:

- Markdown files, including instruction and skill files (`.md`).
- Comment blocks in shell scripts.
- YAML and XML comments.

Code blocks, tables and URLs may exceed the limit. Do not break URLs across lines.

## Use only ASCII characters
<!-- check: No non-ASCII characters (no em-dashes, special arrows, etc.) -->

Use only ASCII characters. Do not use em-dashes, unicode arrows, or other
non-ASCII symbols. Use `->` for arrows. Avoid dashes used as parenthetical
separators (including the ASCII `--` form); rephrase using commas, semicolons,
or parentheses instead.

## Punctuation in lists
<!-- check: Enumerated items end with a period -->

Enumerated and bulleted list items end with a period. Exception: items that are
single words or short noun phrases (e.g., items in a definition list or table
of contents).

## Markdown links
<!-- check: Links use reference style (`[text][ref]`, not `[text](url)`) -->
<!-- check: Link reference definitions are at the bottom of the file -->
<!-- check: Link reference definitions are in lexicographic order -->

Use reference-style links throughout: `[text][ref]` in prose, with `[ref]: url`
definitions collected exclusively at the bottom of the file. Do not use inline
links or scatter definitions through the document.

**Do:**

```markdown
See [`java-style.instructions.md`][java-style] for Java conventions.

[java-style]: java-style.instructions.md
```

**Don't:**

```markdown
See [`java-style.instructions.md`](java-style.instructions.md) for Java
conventions.
```

Order all reference definitions lexicographically by reference name.

### Link reference naming
<!-- check: Link reference names follow the naming convention -->

Name link references by category:

- **Instruction files**: filename stem without `.instructions.md` (e.g.,
  `[bug-checkers]` for `bug-checkers.instructions.md`).
- **Skill files**: `skill-` prefix + skill name (e.g., `[skill-bug-checker]`
  for `.agents/skills/bug-checker/SKILL.md`).
- **Other files**: descriptive lowercase name (e.g., `[agent-md]` for
  `AGENT.md`).

Do not abbreviate reference names. Use the full name for consistency.

## Single space after sentences
<!-- check: Single space after sentences (not two) -->

Use a single space after a period, not two.

## `XXX` comments for future work
<!-- check: Use `XXX:` for future work (not `TODO:` or `FIXME:`) -->

Use `XXX:` (not `TODO:` or `FIXME:`) to mark future work in all file types. In
Markdown, use `<!-- XXX: ... -->`. In shell scripts, use `# XXX: ...`. In Java,
use `// XXX: ...`.

## Respect `.editorconfig`
<!-- check: skip -->

This repository includes an `.editorconfig` file. Configure your editor to
respect it. It enforces indentation style, trailing whitespace removal, and
final newlines.

[java-style]: java-style.instructions.md
