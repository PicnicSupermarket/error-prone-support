# OpenAI Coder

A command line utility that queries OpenAI for code fixes based on error and
warning messages extracted from Maven build output.

## Open topics and ideas.

* Add support for an interactive mode, in which the user is prompted to
  confirm an identified issue before sending it to OpenAI.
    * In case multiple issues are reported against a single file, the user
      should be able to (a) select which issue(s) to send to OpenAI and (b)
      whether to batch the requests.
    * The user should be able to accept or reject the OpenAI response.
    * The user should be able to (re)try with a higher or lower temperature.
    * If a change is accepted, the user may opt to resubmit the modified file
      with a (new) subset of the reported issues.
    * The user should be able to modify the OpenAI request payload before
      sending it.

  (Check whether this can be done with Picocli (see below); otherwise check
  out [JLine](https://jline.github.io/jline3/).)
* Integrate with [Picocli](https://picocli.info/) (or similar) to make the
  command line interface more user-friendly.
    * Add a `--dry-run` flag that only prints the issues that would be reported
      to OpenAI.
    * Add a `--verbose` flag that prints the OpenAI request and response
      payloads, or some abstraction thereof.
    * Add a `--no-interactive` flag that does not prompt the user to confirm
      the issue before sending it to OpenAI.
    * Add flags to include/exclude certain types of issues and files from being
      processed.
    * Add a `--help` flag that prints a help message.
    * Add a `--run-to-fix <command>` (name TBD) flag that repeatedly runs the
      given command in a sub-process and processes the output until either no
      further issues are reported, or no further fixes are found.
* Create a binary image using [GraalVM](https://www.graalvm.org/).
* Add support for sending a suitable subset of the code to OpenAI, so as (a) to
  better deal with the token limit and (b) potentially reduce cost. This might
  take the form of using the line numbers of the issue to extract code section,
  as well as any relevant context. E.g. by replacing the source of irrelevant
  members with a unique placeholder. When the response is received, the
  placeholders are replaced with the original code.
* Add support for parsing other formats (next to Maven build output). E.g.
  Sarif.
* Add support for a mode in which a file (or if we can pull it off: a group of
  files) is explicitly specified to be processed using instructions entered
  interactively or non-interactively.
* Introduce an `IssueExtractor` for Error Prone test compiler output.
* Write an `IssueExtractor` for the output of the `dependency:analyze` Maven
  goal:
  ```
  [INFO] --- dependency:3.5.0:analyze-only (analyze-dependencies) @ openai-coder ---
  [WARNING] Used undeclared dependencies found:
  [WARNING]    com.google.code.findbugs:jsr305:jar:3.0.2:compile
  [WARNING]    com.theokanning.openai-gpt3-java:api:jar:0.12.0:compile
  [WARNING]    org.junit.jupiter:junit-jupiter-api:jar:5.9.2:test
  [WARNING]    io.github.java-diff-utils:java-diff-utils:jar:4.0:compile
  [WARNING] Unused declared dependencies found:
  [WARNING]    com.fasterxml.jackson.core:jackson-databind:jar:2.14.2:compile
  [WARNING]    tech.picnic.error-prone-support:refaster-runner:jar:0.9.1-SNAPSHOT:compile
  [WARNING]    com.google.errorprone:error_prone_check_api:jar:2.18.0:compile
  [WARNING]    com.google.errorprone:error_prone_annotation:jar:2.18.0:compile
  [WARNING]    com.google.errorprone:error_prone_test_helpers:jar:2.18.0:compile
  [WARNING]    org.springframework:spring-core:jar:5.3.26:compile
  ```
  This could e.g. be transformed to:
  ```
  [INFO] --- dependency:3.5.0:analyze-only (analyze-dependencies) @ openai-coder ---
  [WARNING] Used undeclared dependencies found:
  [WARNING]    com.google.code.findbugs:jsr305:jar:3.0.2:compile
  [WARNING]    com.theokanning.openai-gpt3-java:api:jar:0.12.0:compile
  [WARNING]    org.junit.jupiter:junit-jupiter-api:jar:5.9.2:test
  [WARNING]    io.github.java-diff-utils:java-diff-utils:jar:4.0:compile
  [WARNING] Unused declared dependencies found:
  [WARNING]    com.fasterxml.jackson.core:jackson-databind:jar:2.14.2:compile
  [WARNING]    tech.picnic.error-prone-support:refaster-runner:jar:0.9.1-SNAPSHOT:compile
  [WARNING]    com.google.errorprone:error_prone_check_api:jar:2.18.0:compile
  [WARNING]    com.google.errorprone:error_prone_annotation:jar:2.18.0:compile
  [WARNING]    com.google.errorprone:error_prone_test_helpers:jar:2.18.0:compile
  [WARNING]    org.springframework:spring-core:jar:5.3.26:compile
  ```
  (Though `type` and `version` can then conditionally be omitted.)

  The problem: the relevant `pom.xml` file is not available in the Maven
  output. It could be inferred from the preceding `dependency:analyze` line,
  but that line is logged at the `INFO` level, which is not extracted by
  the `LogLineExtractor`.
