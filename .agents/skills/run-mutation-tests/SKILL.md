---
name: run-mutation-tests
description: >
  Use this skill when running mutation tests or interpreting mutation test
  results. Covers running Pitest, interpreting surviving mutants, and
  documenting unkillable mutations.
---

# Run Mutation Tests

Use this skill after tests pass, to verify mutation test coverage.

Read [`.github/instructions/testing.instructions.md`][testing] for the mutation
testing conventions. Then:

1. **Run mutation tests**: `./run-branch-mutation-tests.sh`.
2. **Find surviving mutants** in `<module>/target/pit-reports/mutations.csv`.
   Focus on `SURVIVED` and `NO_COVERAGE` entries.
3. **For each surviving mutant**, either:
   - Add a test that detects the mutation.
   - Simplify the production code if the mutated path is unreachable.
   - Document with `// XXX:` if genuinely unkillable (see testing conventions).
4. **Re-run** to verify that all killable mutants are killed.
5. **Commit** any changes.

[testing]: ../../../.github/instructions/testing.instructions.md
