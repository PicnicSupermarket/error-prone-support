package tech.picnic.errorprone.refaster.runner;

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

final class RefasterTest {
  private static final Pattern DIAGNOSTIC_STRING_OF_SIZE_ZERO =
      Pattern.compile(
          "\\[Refaster Rule\\] FooRules\\.StringOfSizeZeroRule: Refactoring opportunity\\s+.+\\s+");
  private static final Pattern DIAGNOSTIC_STRING_OF_SIZE_ONE =
      Pattern.compile(
          "\\[Refaster Rule\\] FooRules\\.StringOfSizeOneRule: "
              + "A custom description about matching single-char strings\\s+.+\\s+"
              + "\\(see https://error-prone.picnic.tech/refasterrules/FooRules#StringOfSizeOneRule\\)");
  private static final Pattern DIAGNOSTIC_STRING_OF_SIZE_TWO =
      Pattern.compile(
          "\\[Refaster Rule\\] FooRules\\.ExtraGrouping\\.StringOfSizeTwoRule: "
              + "A custom subgroup description\\s+.+\\s+"
              + "\\(see https://example.com/rule/FooRules#ExtraGrouping.StringOfSizeTwoRule\\)");
  private static final Pattern DIAGNOSTIC_STRING_OF_SIZE_THREE =
      Pattern.compile(
          "\\[Refaster Rule\\] FooRules\\.ExtraGrouping\\.StringOfSizeThreeRule: "
              + "A custom description about matching three-char strings\\s+.+\\s+"
              + "\\(see https://example.com/custom\\)");

  @Test
  void identification() {
    CompilationTestHelper.newInstance(Refaster.class, getClass())
        .matchAllDiagnostics()
        .expectErrorMessage("StringOfSizeZeroRule", DIAGNOSTIC_STRING_OF_SIZE_ZERO.asPredicate())
        .expectErrorMessage("StringOfSizeOneRule", DIAGNOSTIC_STRING_OF_SIZE_ONE.asPredicate())
        .expectErrorMessage("StringOfSizeTwoRule", DIAGNOSTIC_STRING_OF_SIZE_TWO.asPredicate())
        .expectErrorMessage("StringOfSizeThreeRule", DIAGNOSTIC_STRING_OF_SIZE_THREE.asPredicate())
        .addSourceLines(
            "A.java",
            """
            class A {
              void m() {
                // BUG: Diagnostic matches: StringOfSizeZeroRule
                boolean b1 = "foo".toCharArray().length == 0;
                // BUG: Diagnostic matches: StringOfSizeOneRule
                boolean b2 = "bar".toCharArray().length == 1;
                // BUG: Diagnostic matches: StringOfSizeTwoRule
                boolean b3 = "baz".toCharArray().length == 2;
                // BUG: Diagnostic matches: StringOfSizeThreeRule
                boolean b4 = "qux".toCharArray().length == 3;
              }
            }
            """)
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
    return Stream.of(
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
            ImmutableList.of("-Xep:Refaster:ERROR"), ImmutableList.of(ERROR, ERROR, ERROR, ERROR)),
        arguments(
            ImmutableList.of("-XepAllErrorsAsWarnings"),
            ImmutableList.of(defaultSeverity, WARNING, WARNING, SUGGESTION)),
        arguments(
            ImmutableList.of("-Xep:Refaster:OFF", "-XepAllErrorsAsWarnings"), ImmutableList.of()),
        arguments(
            ImmutableList.of("-Xep:Refaster:DEFAULT", "-XepAllErrorsAsWarnings"),
            ImmutableList.of(defaultSeverity, WARNING, WARNING, SUGGESTION)),
        arguments(
            ImmutableList.of("-Xep:Refaster:WARN", "-XepAllErrorsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
        arguments(
            ImmutableList.of("-Xep:Refaster:ERROR", "-XepAllErrorsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
        arguments(
            ImmutableList.of("-XepAllSuggestionsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, ERROR, WARNING)),
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
            ImmutableList.of("-XepAllErrorsAsWarnings", "-XepAllSuggestionsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
        arguments(
            ImmutableList.of(
                "-Xep:Refaster:OFF", "-XepAllErrorsAsWarnings", "-XepAllSuggestionsAsWarnings"),
            ImmutableList.of()),
        arguments(
            ImmutableList.of(
                "-Xep:Refaster:DEFAULT", "-XepAllErrorsAsWarnings", "-XepAllSuggestionsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
        arguments(
            ImmutableList.of(
                "-Xep:Refaster:WARN", "-XepAllErrorsAsWarnings", "-XepAllSuggestionsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)),
        arguments(
            ImmutableList.of(
                "-Xep:Refaster:ERROR", "-XepAllErrorsAsWarnings", "-XepAllSuggestionsAsWarnings"),
            ImmutableList.of(WARNING, WARNING, WARNING, WARNING)));
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
    CompilationTestHelper compilationTestHelper =
        CompilationTestHelper.newInstance(Refaster.class, getClass())
            .setArgs(arguments)
            .addSourceLines(
                "A.java",
                """
                class A {
                  void m() {
                    boolean[] bs = {
                      "foo".toCharArray().length == 0,
                      "bar".toCharArray().length == 1,
                      "baz".toCharArray().length == 2,
                      "qux".toCharArray().length == 3
                    };
                  }
                }
                """);

    if (expectedSeverities.isEmpty()) {
      compilationTestHelper.doTest();
    } else {
      assertThatThrownBy(compilationTestHelper::doTest)
          .isInstanceOf(AssertionError.class)
          .message()
          .satisfies(
              message ->
                  assertThat(extractRefasterSeverities("A.java", message))
                      .containsExactlyElementsOf(expectedSeverities));
    }
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
    return switch (compilerDiagnosticsPrefix) {
      case "Note" -> SUGGESTION;
      case "warning" -> WARNING;
      case "error" -> ERROR;
      default ->
          throw new IllegalStateException(
              String.format("Unrecognized diagnostics prefix '%s'", compilerDiagnosticsPrefix));
    };
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass())
        .addInputLines(
            "A.java",
            """
            class A {
              void m() {
                boolean b1 = "foo".toCharArray().length == 0;
                boolean b2 = "bar".toCharArray().length == 1;
                boolean b3 = "baz".toCharArray().length == 2;
                boolean b4 = "qux".toCharArray().length == 3;
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            class A {
              void m() {
                boolean b1 = "foo".isEmpty();
                boolean b2 = "bar".length() == 1;
                boolean b3 = "baz".length() == 2;
                boolean b4 = "qux".length() == 3;
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void restrictedReplacement() {
    BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass())
        .setArgs(
            "-XepOpt:Refaster:NamePattern=.*\\$(StringOfSizeZeroVerboseRule|StringOfSizeTwoRule)$")
        .addInputLines(
            "A.java",
            """
            class A {
              void m() {
                boolean b1 = "foo".toCharArray().length == 0;
                boolean b2 = "bar".toCharArray().length == 1;
                boolean b3 = "baz".toCharArray().length == 2;
                boolean b4 = "qux".toCharArray().length == 3;
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            class A {
              void m() {
                boolean b1 = "foo".length() + 1 == 1;
                boolean b2 = "bar".toCharArray().length == 1;
                boolean b3 = "baz".length() == 2;
                boolean b4 = "qux".toCharArray().length == 3;
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
