---
name: create-refaster-rule
description: >
  Use this skill when asked to create or modify Refaster rules in this
  repository. It covers choosing the right rule collection, updating test
  fixtures and registration, and following the repository validation workflow.
---

# Create Refaster Rule

Use this skill for requests to add or change files in `*/refasterrules/*`.

Read [`.github/instructions/refaster-rules.instructions.md`][refaster-rules]
for the full conventions and step-by-step guide. Then:

1. **Determine the target collection.** Based on the topic of the requested
   rule(s), decide whether to add to an existing `{Topic}Rules.java` file or
   create a new one.
2. **Create or modify the rule file** following Step 1 (and Step 2 for advanced
   patterns) from the instructions.
3. **Create or modify the test input file** following Step 3.
4. **Create or modify the test output file** following Step 4.
5. **Register the collection** (if new) following Step 5.
6. **Verify** by running the tests as described in Step 6.
7. **Review** all changes against the applicable sections of
   [`.github/instructions/review.instructions.md`][review], and go over the
   instructions line-by-line to validate that all requirements are met.
8. **Apply the new rule** using the commands in Step 7.

[refaster-rules]: ../../../.github/instructions/refaster-rules.instructions.md
[review]: ../../../.github/instructions/review.instructions.md
