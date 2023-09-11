package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
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
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: true, GUAVA: true, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void isIntroductionAllowedWitnessClassesInSymtab() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
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
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .withClasspath(ImmutableList.class, Flux.class)
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: false, GUAVA: true, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void isIntroductionAllowedWitnessClassesNotOnClassPath() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
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
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
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

  /**
   * Flags classes with a diagnostics message that indicates, for each {@link ThirdPartyLibrary}
   * element, whether they can be used.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `ThirdPartyLibrary` for testing purposes")
  public static final class TestChecker extends BugChecker implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              Arrays.stream(ThirdPartyLibrary.values())
                  .map(
                      lib ->
                          String.join(
                              ": ", lib.name(), String.valueOf(lib.isIntroductionAllowed(state))))
                  .collect(joining(", ")))
          .build();
    }
  }
}
