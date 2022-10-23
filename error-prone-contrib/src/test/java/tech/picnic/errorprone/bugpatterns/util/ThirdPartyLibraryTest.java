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
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestChecker.class, getClass());

  @Test
  void canUse() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: true, GUAVA: true, NEW_RELIC_AGENT_API: true, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void canUseWitnessClassesInSymtab() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.newrelic.api.agent.Agent;",
            "import org.assertj.core.api.Assertions;",
            "import reactor.core.publisher.Flux;",
            "",
            "// BUG: Diagnostic contains: ASSERTJ: true, GUAVA: true, NEW_RELIC_AGENT_API: true, REACTOR: true",
            "class A {",
            "  void m(Class<?> clazz) {",
            "    m(Assertions.class);",
            "    m(ImmutableList.class);",
            "    m(Agent.class);",
            "    m(Flux.class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void canUseWitnessClassesPartiallyOnClassPath() {
    compilationTestHelper
        .withClasspath(ImmutableList.class, Flux.class)
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: false, GUAVA: true, NEW_RELIC_AGENT_API: false, REACTOR: true",
            "class A {}")
        .doTest();
  }

  @Test
  void canUseWitnessClassesNotOnClassPath() {
    compilationTestHelper
        .withClasspath()
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: ASSERTJ: false, GUAVA: false, NEW_RELIC_AGENT_API: false, REACTOR:",
            "// false",
            "class A {}")
        .doTest();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void canUseIgnoreClasspathCompat(boolean ignoreClassPath) {
    compilationTestHelper
        .setArgs("-XepOpt:ErrorProneSupport:IgnoreClasspathCompat=" + ignoreClassPath)
        .withClasspath(ImmutableList.class, Flux.class)
        .addSourceLines(
            "A.java",
            String.format(
                "// BUG: Diagnostic contains: ASSERTJ: %s, GUAVA: true, NEW_RELIC_AGENT_API: %s, REACTOR: true",
                ignoreClassPath, ignoreClassPath),
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
                  .map(lib -> String.join(": ", lib.name(), String.valueOf(lib.canUse(state))))
                  .collect(joining(", ")))
          .build();
    }
  }
}
