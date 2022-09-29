package tech.picnic.errorprone.refaster.runner;

import static com.google.common.base.Predicates.containsPattern;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class RefasterTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(Refaster.class, getClass())
          .matchAllDiagnostics()
          .expectErrorMessage(
              "StringOfSizeZeroTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeZeroTemplate: Refactoring opportunity\\s+.+\\s+null"))
          .expectErrorMessage(
              "StringOfSizeOneTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeOneTemplate: "
                      + "A custom description about matching single-char strings\\s+.+\\s+"
                      + "\\(see https://error-prone.picnic.tech/refastertemplates/FooTemplates#StringOfSizeOneTemplate\\)"))
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

  private static Stream<Arguments> reportedSeverityTestCases() {
    /* { arguments, expectedSeverities } */

    Stream<Arguments> forkTestCases =
        isBuiltWithErrorProneFork()
            ? Stream.of(
                arguments(
                    ImmutableList.of("-Xep:Refaster:OFF", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of()),
                arguments(
                    ImmutableList.of("-Xep:Refaster:DEFAULT", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of("warning", "warning", "error", "warning")),
                arguments(
                    ImmutableList.of("-Xep:Refaster:WARN", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of("warning", "warning", "warning", "warning")),
                arguments(
                    ImmutableList.of("-Xep:Refaster:ERROR", "-XepAllSuggestionsAsWarnings"),
                    ImmutableList.of("error", "error", "error", "error")))
            : Stream.empty();

    return Stream.concat(
        Stream.of(
            arguments(ImmutableList.of(), ImmutableList.of("Note", "warning", "error", "Note")),
            arguments(ImmutableList.of("-Xep:Refaster:OFF"), ImmutableList.of()),
            arguments(
                ImmutableList.of("-Xep:Refaster:DEFAULT"),
                ImmutableList.of("Note", "warning", "error", "Note")),
            arguments(
                ImmutableList.of("-Xep:Refaster:WARN"),
                ImmutableList.of("warning", "warning", "warning", "warning")),
            arguments(
                ImmutableList.of("-Xep:Refaster:ERROR"),
                ImmutableList.of("error", "error", "error", "error")),
            arguments(
                ImmutableList.of("-XepAllErrorsAsWarnings"),
                ImmutableList.of("Note", "warning", "warning", "Note")),
            arguments(
                ImmutableList.of("-Xep:Refaster:OFF", "-XepAllErrorsAsWarnings"),
                ImmutableList.of()),
            arguments(
                ImmutableList.of("-Xep:Refaster:DEFAULT", "-XepAllErrorsAsWarnings"),
                ImmutableList.of("Note", "warning", "warning", "Note")),
            arguments(
                ImmutableList.of("-Xep:Refaster:WARN", "-XepAllErrorsAsWarnings"),
                ImmutableList.of("warning", "warning", "warning", "warning")),
            arguments(
                ImmutableList.of("-Xep:Refaster:ERROR", "-XepAllErrorsAsWarnings"),
                ImmutableList.of("warning", "warning", "warning", "warning"))),
        forkTestCases);
  }

  /**
   * Verifies that the bug checker flags the refactoring opportunities with the appropriate severity
   * level.
   *
   * @implNote This test setup is rather cumbersome, because the {@link CompilationTestHelper} does
   *     not enable direct assertions against the severity of collected diagnostics output.
   */
  @MethodSource("reportedSeverityTestCases")
  @ParameterizedTest
  void defaultSeverities(
      ImmutableList<String> arguments, ImmutableList<String> expectedSeverities) {
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

  private static ImmutableList<String> extractRefasterSeverities(String fileName, String message) {
    return Pattern.compile(
            String.format(
                "/%s:(\\d+): (Note|warning|error): \\[Refaster Rule\\]", Pattern.quote(fileName)))
        .matcher(message)
        .results()
        .collect(
            toImmutableSortedMap(
                naturalOrder(), r -> Integer.parseInt(r.group(1)), r -> r.group(2)))
        .values()
        .asList();
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

  private static boolean isBuiltWithErrorProneFork() {
    Class<?> clazz;
    try {
      clazz =
          Class.forName(
              "com.google.errorprone.ErrorProneOptions",
              /* initialize= */ false,
              Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Can't load `ErrorProneOptions` class", e);
    }
    return Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .anyMatch(m -> m.getName().equals("isSuggestionsAsWarnings"));
  }
}
