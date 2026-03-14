Create one or more new Refaster rules based on the following description:

$ARGUMENTS

## Instructions

Read `.github/instructions/refaster-rules.instructions.md` for the full
conventions and step-by-step guide. Then:

1. **Determine the target collection.** Based on the topic of the requested
   rule(s), decide whether to add to an existing `{Topic}Rules.java` file or
   create a new one.
2. **Create or modify the rule file** following Step 1 (and Step 2 for advanced
   patterns) from the instructions.
3. **Create or modify the test input file** following Step 3.
4. **Create or modify the test output file** following Step 4.
5. **Register the collection** (if new) following Step 5.
6. **Verify** by running the tests as described in Step 6.
7. **Self-review checklist** before committing:
   - [ ] Javadoc qualifier matches the transformation semantics (see qualifier
     decision guide in the instructions). Do not copy from adjacent rules.
   - [ ] No fully qualified types in test files — all types are imported.
   - [ ] Test data is minimal: single-element collections, simple comparators,
     no unnecessary complexity.
8. **Follow the workflow** described in `AGENT.md` (mutation tests, apply
suggestions, full build).
