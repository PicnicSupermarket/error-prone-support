---
name: apply-self-check
description: >
  Use this skill when applying Error Prone auto-fixes to the codebase and
  resolving remaining build warnings. Covers running the self-check script,
  validating changes, and efficiently batch-fixing warnings.
---

# Apply Self-Check

Use this skill to apply automated fixes and resolve build warnings.

1. **Apply auto-fixes**: Run `./apply-error-prone-suggestions.sh`. Inspect the
   diff with `git diff` and validate that changes are correct. If changes look
   wrong, undo them and fix the underlying bug.
2. **Commit** auto-fix changes (if any).
3. **Full build**: Run `./run-full-build.sh`.
4. If the build **fails**, run `mvn clean verify -Pself-check -DskipTests
   -Dverification.warn` to collect all warnings. Ignore warnings that are
   non-actionable or unrelated to the current change. **Batch-fix** the
   remaining warnings in one pass, then re-run `./run-full-build.sh`.
5. **Repeat** steps 3-4 until the build passes.
6. **Commit** any manual fixes.
