package tech.picnic.errorprone.refaster.runner;

import static com.google.common.base.Predicates.containsPattern;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerInfo;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CompilationTestHelper;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.ErrorProneFork;

final class RefasterTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(Refaster.class, getClass())
          .matchAllDiagnostics()
          .expectErrorMessage(
              "StringOfSizeZeroTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeZeroTemplate: Refactoring opportunity\\s+.+\\s+"))
          .expectErrorMessage(
              "StringOfSizeOneTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeOneTemplate: "
                      + "A custom description about matching single-char strings\\s+.+\\s+"
                      + "\\(see https://error-prone.picnic.tech/refasterrules/FooTemplates#StringOfSizeOneTemplate\\)"))
          .expectErrorMessage(
              "StringOfSizeTwoTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.ExtraGrouping\\.StringOfSizeTwoTemplate: "
                      + "A custom subgroup description\\s+.+\\s+"
                      + "\\(see https://example.com/template/FooTemplates#ExtraGrouping.StringOfSizeTwoTemplate\\)"))
          .expectErrorMessage(
              "StringOfSizeThreeTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.ExtraGrouping\\.StringOfSizeThreeTemplate: "
                      + "A custom description about matching three-char strings\\s+.+\\s+"
                      + "\\(see https://example.com/custom\\)"));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass());
  private final BugCheckerRefactoringTestHelper restrictedRefactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass())
          .setArgs(
              "-XepOpt:Refaster:NamePattern=.*\\$(StringOfSizeZeroVerboseTemplate|StringOfSizeTwoTemplate)$");

  @Test
  void identification() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic matches: StringOfSizeZeroTemplate",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "    // BUG: Diagnostic matches: StringOfSizeOneTemplate",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    // BUG: Diagnostic matches: StringOfSizeTwoTemplate",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "    // BUG: Diagnostic matches: StringOfSizeThreeTemplate",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .doTest();
  }

  private static Stream<Arguments> severityAssignmentTestCases() {
    /*
     * The _actual_ default severity is assigned by the `CodeTransformer`s to which the `Refaster`
     * bug checker delegates. Here we verify that the absence of an `@Severity` annotation yields
     * the same severity as the bug checker's declared severity.
     */
    SeverityLevel defaultSeverity = BugCheckerInfo.create(Refaster.class).defaultSeverity();

    /* { arguments, expectedSeverities } */
    return Stream.concat(
        Stream.of(
            arguments(
                ImmutableList.of(), ImmutableList.of(defaultSeverity, WARNING, ERROR, SUGGESTION)),
            arguments(ImmutableList.of("-Xep:Refaster:OFF"), ImmutableList.of()),
            arguments(
                ImmutableList.of("-Xep:Refaster:DEFAULT"),
                ImmutableList.of(defaultSeverity, WARNING, ERROR, SUGGESTION)),
            arguments(
                ImmutableList.of("-Xep:Refaster:WARN"),
                ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
            arguments(
                ImmutableList.of("-Xep:Refaster:ERROR"),
                ImmutableList.of(ERROR, ERROR, ERROR, ERROR)),
            arguments(
                ImmutableList.of("-XepAllErrorsAsWarnings"),
                ImmutableList.of(defaultSeverity, WARNING, WARNING, SUGGESTION)),
            arguments(
                ImmutableList.of("-Xep:Refaster:OFF", "-XepAllErrorsAsWarnings"),
                ImmutableList.of()),
            arguments(
                ImmutableList.of("-Xep:Refaster:DEFAULT", "-XepAllErrorsAsWarnings"),
                ImmutableList.of(defaultSeverity, WARNING, WARNING, SUGGESTION)),
            arguments(
                ImmutableList.of("-Xep:Refaster:WARN", "-XepAllErrorsAsWarnings"),
                ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
            arguments(
                ImmutableList.of("-Xep:Refaster:ERROR", "-XepAllErrorsAsWarnings"),
                ImmutableList.of(WARNING, WARNING, WARNING, WARNING))),
        ErrorProneFork.isErrorProneForkAvailable()
            ? Stream.of(
                arguments(
                    ImmutableList.of("-Xep:Refaster:OFF", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of()),
                arguments(
                    ImmutableList.of("-Xep:Refaster:DEFAULT", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(WARNING, WARNING, ERROR, WARNING)),
                arguments(
                    ImmutableList.of("-Xep:Refaster:WARN", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
                arguments(
                    ImmutableList.of("-Xep:Refaster:ERROR", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(ERROR, ERROR, ERROR, ERROR)),
                arguments(
                    ImmutableList.of(
                        "-Xep:Refaster:OFF",
                        "-XepAllErrorsAsWarnings",
                        "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of()),
                arguments(
                    ImmutableList.of(
                        "-Xep:Refaster:DEFAULT",
                        "-XepAllErrorsAsWarnings",
                        "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
                arguments(
                    ImmutableList.of(
                        "-Xep:Refaster:WARN",
                        "-XepAllErrorsAsWarnings",
                        "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
                arguments(
                    ImmutableList.of(
                        "-Xep:Refaster:ERROR",
                        "-XepAllErrorsAsWarnings",
                        "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of(WARNING, WARNING, WARNING, WARNING)))
            : Stream.empty());
  }

  /**
   * Verifies that the bug checker flags refactoring opportunities with the appropriate severity
   * level.
   *
   * @implNote This test setup is rather cumbersome, because {@link CompilationTestHelper} does not
   *     enable direct assertions against the severity of collected diagnostics output.
   */
  @MethodSource("severityAssignmentTestCases")
  @ParameterizedTest
  void severityAssignment(
      ImmutableList<String> arguments, ImmutableList<SeverityLevel> expectedSeverities) {
    assertThatThrownBy(
            () ->
                compilationHelper
                    .setArgs(arguments)
                    .addSourceLines(
                        "A.java",
                        "class A {",
                        "  void m() {",
                        "    boolean[] bs = {",
                        "      \"foo\".toCharArray().length == 0,",
                        "      \"bar\".toCharArray().length == 1,",
                        "      \"baz\".toCharArray().length == 2,",
                        "      \"qux\".toCharArray().length == 3",
                        "    };",
                        "  }",
                        "}")
                    .doTest())
        .isInstanceOf(AssertionError.class)
        .message()
        .satisfies(
            message ->
                assertThat(extractRefasterSeverities("A.java", message))
                    .containsExactlyElementsOf(expectedSeverities));
  }

  private static ImmutableList<SeverityLevel> extractRefasterSeverities(
      String fileName, String message) {
    return Pattern.compile(
            String.format(
                "/%s:(\\d+): (Note|warning|error): \\[Refaster Rule\\]", Pattern.quote(fileName)))
        .matcher(message)
        .results()
        .sorted(comparingInt(r -> Integer.parseInt(r.group(1))))
        .map(r -> toSeverityLevel(r.group(2)))
        .collect(toImmutableList());
  }

  private static SeverityLevel toSeverityLevel(String compilerDiagnosticsPrefix) {
    switch (compilerDiagnosticsPrefix) {
      case "Note":
        return SUGGESTION;
      case "warning":
        return WARNING;
      case "error":
        return ERROR;
      default:
        throw new IllegalStateException(
            String.format("Unrecognized diagnostics prefix '%s'", compilerDiagnosticsPrefix));
    }
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".isEmpty();",
            "    boolean b2 = \"bar\".length() == 1;",
            "    boolean b3 = \"baz\".length() == 2;",
            "    boolean b4 = \"qux\".length() == 3;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void restrictedReplacement() {
    restrictedRefactoringTestHelper
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".length() + 1 == 1;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".length() == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
