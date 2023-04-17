package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.stream.Stream;

// XXX: It would be nice to also handle this case:
//
// [INFO] --- dependency:3.5.0:analyze-only (analyze-dependencies) @ openai-coder ---
// [WARNING] Used undeclared dependencies found:
// [WARNING]    com.google.code.findbugs:jsr305:jar:3.0.2:compile
// [WARNING]    com.theokanning.openai-gpt3-java:api:jar:0.12.0:compile
// [WARNING]    org.junit.jupiter:junit-jupiter-api:jar:5.9.2:test
// [WARNING]    io.github.java-diff-utils:java-diff-utils:jar:4.0:compile
// [WARNING] Unused declared dependencies found:
// [WARNING]    com.fasterxml.jackson.core:jackson-databind:jar:2.14.2:compile
// [WARNING]    tech.picnic.error-prone-support:refaster-runner:jar:0.9.1-SNAPSHOT:compile
// [WARNING]    com.google.errorprone:error_prone_check_api:jar:2.18.0:compile
// [WARNING]    com.google.errorprone:error_prone_annotation:jar:2.18.0:compile
// [WARNING]    com.google.errorprone:error_prone_test_helpers:jar:2.18.0:compile
// [WARNING]    org.springframework:spring-core:jar:5.3.26:compile
//
// This could e.g. be transformed to:
// ---
// [INFO] --- dependency:3.5.0:analyze-only (analyze-dependencies) @ openai-coder ---
// [WARNING] Used undeclared dependencies found:
// [WARNING]    com.google.code.findbugs:jsr305:jar:3.0.2:compile
// [WARNING]    com.theokanning.openai-gpt3-java:api:jar:0.12.0:compile
// [WARNING]    org.junit.jupiter:junit-jupiter-api:jar:5.9.2:test
// [WARNING]    io.github.java-diff-utils:java-diff-utils:jar:4.0:compile
// [WARNING] Unused declared dependencies found:
// [WARNING]    com.fasterxml.jackson.core:jackson-databind:jar:2.14.2:compile
// [WARNING]    tech.picnic.error-prone-support:refaster-runner:jar:0.9.1-SNAPSHOT:compile
// [WARNING]    com.google.errorprone:error_prone_check_api:jar:2.18.0:compile
// [WARNING]    com.google.errorprone:error_prone_annotation:jar:2.18.0:compile
// [WARNING]    com.google.errorprone:error_prone_test_helpers:jar:2.18.0:compile
// [WARNING]    org.springframework:spring-core:jar:5.3.26:compile
// ---
// (Though `type` and `version` can then conditionally be omitted.)
//
// The problem: the relevant `pom.xml` file is not available in the Maven output. It could be
// inferred from the preceding `dependency:analyze` line, but that line is logged at the `INFO`
// level, which is not extracted by the `LogLineExtractor`.

// XXX: Document
interface IssueExtractor<F> {
  Stream<Issue<F>> extract(String str);

  // XXX: Move to separate file?
  // XXX: `file` as first argument? (If so, also swap method order.)
  record Issue<F>(String message, F file, int line, OptionalInt column) {
    Issue<F> withMessage(String message) {
      return new Issue<>(message, file, line, column);
    }

    <T> Issue<T> withFile(T file) {
      return new Issue<>(message, file, line, column);
    }

    String description() {
      return column().isEmpty()
          ? String.format("Line %s: %s", line(), message())
          : String.format("Line %s, column %s: %s", line(), column().getAsInt(), message());
    }
  }
}
