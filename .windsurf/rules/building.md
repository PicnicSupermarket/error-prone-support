# Build instructions

- Build using Maven.
- While developing new code, run with `-Dverification.skip`.
- Once all tests pass, run `./apply-error-prone-suggestions.sh`. This will
  automatically resolve a large subset of possible lint warnings.
- Finally, rerun the build with `-Dverification.warn` and try to resolve
  reported warnings.
