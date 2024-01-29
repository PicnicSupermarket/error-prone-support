package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

final class ThirdPartyLibraryTest {
  @Test
  void isIntroductionAllowed() {
    CompilationTestHelper.newInstance(IsIntroductionAllowedTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: true, GUAVA: true, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void isIntroductionAllowedWitnessClassesInSymbolTable() {
    CompilationTestHelper.newInstance(IsIntroductionAllowedTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import org.assertj.core.api.Assertions;",
            "import reactor.core.publisher.Flux;",
            "",
            "// BUG: Diagnostic contains: ASSERTJ: true, GUAVA: true, REACTOR: true",
            "class A {",
            "  void m(Class<?> clazz) {",
            "    m(Assertions.class);",
            "    m(ImmutableList.class);",
            "    m(Flux.class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void isIntroductionAllowedWitnessClassesPartiallyOnClassPath() {
    CompilationTestHelper.newInstance(IsIntroductionAllowedTestChecker.class, getClass())
        .withClasspath(ImmutableList.class, Flux.class)
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: false, GUAVA: true, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void isIntroductionAllowedWitnessClassesNotOnClassPath() {
    CompilationTestHelper.newInstance(IsIntroductionAllowedTestChecker.class, getClass())
        .withClasspath()
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: false, GUAVA: false, REACTOR:",
            "// false",
            "class A {}")
        .doTest();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isIntroductionAllowedIgnoreClasspathCompat(boolean ignoreClassPath) {
    CompilationTestHelper.newInstance(IsIntroductionAllowedTestChecker.class, getClass())
        .setArgs("-XepOpt:ErrorProneSupport:IgnoreClasspathCompat=" + ignoreClassPath)
        .withClasspath(ImmutableList.class, Flux.class)
        .addSourceLines(
            "A.java",
            String.format(
                "// BUG: Diagnostic contains: ASSERTJ: %s, GUAVA: true, REACTOR: true",
                ignoreClassPath),
            "class A {}")
        .doTest();
  }

  @Test
  void canIntroduceUsage() {
    CompilationTestHelper.newInstance(CanIntroduceUsageTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: GUAVA_PUBLIC: true, GUAVA_PRIVATE: false, ERROR_PRONE_PUBLIC_NESTED:",
            "// true",
            "class A {}")
        .doTest();
  }

  /**
   * Flags classes with a diagnostics message that indicates, for each {@link ThirdPartyLibrary}
   * element, whether they can be used.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `ThirdPartyLibrary` for testing purposes")
  public static final class IsIntroductionAllowedTestChecker extends BugChecker
      implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              Arrays.stream(ThirdPartyLibrary.values())
                  .map(lib -> lib.name() + ": " + lib.isIntroductionAllowed(state))
                  .collect(joining(", ")))
          .build();
    }
  }

  /**
   * Flags classes with a diagnostics message that indicates, for selected types, the result of
   * {@link ThirdPartyLibrary#canIntroduceUsage(String, VisitorState)}.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `ThirdPartyLibrary` for testing purposes")
  public static final class CanIntroduceUsageTestChecker extends BugChecker
      implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final ImmutableMap<String, String> TYPES =
        ImmutableMap.of(
            "GUAVA_PUBLIC",
            ImmutableList.class.getCanonicalName(),
            "GUAVA_PRIVATE",
            "com.google.common.collect.ImmutableEnumSet",
            "ERROR_PRONE_PUBLIC_NESTED",
            "com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput");

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              TYPES.entrySet().stream()
                  .map(
                      e ->
                          e.getKey()
                              + ": "
                              + ThirdPartyLibrary.canIntroduceUsage(e.getValue(), state))
                  .collect(joining(", ")))
          .build();
    }
  }
}
