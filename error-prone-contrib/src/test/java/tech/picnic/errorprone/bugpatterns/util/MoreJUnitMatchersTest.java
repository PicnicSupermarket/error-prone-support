package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.HAS_METHOD_SOURCE;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.SETUP_OR_TEARDOWN_METHOD;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.TEST_METHOD;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class MoreJUnitMatchersTest {
  @Test
  void methodMatchers() {
    CompilationTestHelper.newInstance(MethodMatchersTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "",
            "class A {",
            "  @BeforeAll",
            "  // BUG: Diagnostic contains: SETUP_OR_TEARDOWN_METHOD",
            "  public void beforeAll() {}",
            "",
            "  @BeforeEach",
            "  @Test",
            "  // BUG: Diagnostic contains: TEST_METHOD, SETUP_OR_TEARDOWN_METHOD",
            "  protected void beforeEachAndTest() {}",
            "",
            "  @AfterEach",
            "  // BUG: Diagnostic contains: SETUP_OR_TEARDOWN_METHOD",
            "  private void afterEach() {}",
            "",
            "  @AfterAll",
            "  // BUG: Diagnostic contains: SETUP_OR_TEARDOWN_METHOD",
            "  private void afterAll() {}",
            "",
            "  @Test",
            "  // BUG: Diagnostic contains: TEST_METHOD",
            "  void test() {}",
            "",
            "  private static Stream<Arguments> booleanArgs() {",
            "    return Stream.of(arguments(false), arguments(true));",
            "  }",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"booleanArgs\")",
            "  // BUG: Diagnostic contains: TEST_METHOD, HAS_METHOD_SOURCE",
            "  void parameterizedTest(boolean b) {}",
            "",
            "  @RepeatedTest(2)",
            "  // BUG: Diagnostic contains: TEST_METHOD",
            "  private void repeatedTest() {}",
            "",
            "  private void unannotatedMethod() {}",
            "}")
        .doTest();
  }

  @Test
  void getMethodSourceFactoryNames() {
    CompilationTestHelper.newInstance(MethodSourceFactoryDescriptorsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "",
            "class A {",
            "  @MethodSource",
            "  // BUG: Diagnostic contains: [matchingMethodSource]",
            "  void matchingMethodSource(boolean b) {}",
            "",
            "  @MethodSource()",
            "  // BUG: Diagnostic contains: [matchingMethodSourceWithParens]",
            "  void matchingMethodSourceWithParens(boolean b) {}",
            "",
            "  @MethodSource(\"\")",
            "  // BUG: Diagnostic contains: [matchingMethodSourceMadeExplicit]",
            "  void matchingMethodSourceMadeExplicit(boolean b) {}",
            "",
            "  @MethodSource({\"\"})",
            "  // BUG: Diagnostic contains: [matchingMethodSourceMadeExplicitWithParens]",
            "  void matchingMethodSourceMadeExplicitWithParens(boolean b) {}",
            "",
            "  @MethodSource({})",
            "  // BUG: Diagnostic contains: []",
            "  void noMethodSources(boolean b) {}",
            "",
            "  @MethodSource(\"myValueFactory\")",
            "  // BUG: Diagnostic contains: [myValueFactory]",
            "  void singleCustomMethodSource(boolean b) {}",
            "",
            "  @MethodSource({\"firstValueFactory\", \"secondValueFactory\"})",
            "  // BUG: Diagnostic contains: [firstValueFactory, secondValueFactory]",
            "  void twoCustomMethodSources(boolean b) {}",
            "",
            "  @MethodSource({\"myValueFactory\", \"\"})",
            "  // BUG: Diagnostic contains: [myValueFactory, customAndMatchingMethodSources]",
            "  void customAndMatchingMethodSources(boolean b) {}",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that flags methods matched by {@link Matcher}s of {@link MethodTree}s
   * exposed by {@link MoreJUnitMatchers}.
   */
  @BugPattern(summary = "Interacts with `MoreJUnitMatchers` for testing purposes", severity = ERROR)
  public static final class MethodMatchersTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final ImmutableMap<String, Matcher<MethodTree>> METHOD_MATCHERS =
        ImmutableMap.of(
            "TEST_METHOD", TEST_METHOD,
            "HAS_METHOD_SOURCE", HAS_METHOD_SOURCE,
            "SETUP_OR_TEARDOWN_METHOD", SETUP_OR_TEARDOWN_METHOD);

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      ImmutableSet<String> matches =
          METHOD_MATCHERS.entrySet().stream()
              .filter(e -> e.getValue().matches(tree, state))
              .map(Map.Entry::getKey)
              .collect(toImmutableSet());

      return matches.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree).setMessage(String.join(", ", matches)).build();
    }
  }

  /**
   * A {@link BugChecker} that flags methods with a JUnit {@code @MethodSource} annotation by
   * enumerating the associated value factory method descriptors.
   */
  @BugPattern(summary = "Interacts with `MoreJUnitMatchers` for testing purposes", severity = ERROR)
  public static final class MethodSourceFactoryDescriptorsTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      AnnotationTree annotation =
          Iterables.getOnlyElement(HAS_METHOD_SOURCE.multiMatchResult(tree, state).matchingNodes());

      return buildDescription(tree)
          .setMessage(
              MoreJUnitMatchers.getMethodSourceFactoryDescriptors(annotation, tree).toString())
          .build();
    }
  }
}
