package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MoreJUnitMatchersTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
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
            "  public void setUp1() {}",
            "",
            "  @BeforeEach",
            "  @Test",
            "  // BUG: Diagnostic contains: TEST_METHOD, SETUP_OR_TEARDOWN_METHOD",
            "  protected void setUp2() {}",
            "",
            "  @AfterEach",
            "  // BUG: Diagnostic contains: SETUP_OR_TEARDOWN_METHOD",
            "  private void tearDown1() {}",
            "",
            "  @AfterAll",
            "  // BUG: Diagnostic contains: SETUP_OR_TEARDOWN_METHOD",
            "  private void tearDown2() {}",
            "",
            "  @Test",
            "  // BUG: Diagnostic contains: TEST_METHOD",
            "  void testFoo() {}",
            "",
            "  private static Stream<Arguments> booleanArgs() {",
            "    return Stream.of(arguments(false), arguments(true));",
            "  }",
            "",
            "  @ParameterizedTest",
            "  @MethodSource",
            "  // BUG: Diagnostic contains: TEST_METHOD, HAS_METHOD_SOURCE",
            "  void testBar() {}",
            "",
            "  @RepeatedTest(2)",
            "  // BUG: Diagnostic contains: TEST_METHOD",
            "  private void qux() {}",
            "",
            "  private void quux() {}",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that delegates to {@link Matcher Matchers} from {@link MoreJUnitMatchers}.
   */
  @BugPattern(
      summary = "Flags methods matched by Matchers from `MoreJUnitMatchers`",
      severity = ERROR)
  public static final class TestChecker extends BugChecker implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      List<String> diagnosticsMessages = new ArrayList<>();

      if (MoreJUnitMatchers.TEST_METHOD.matches(tree, state)) {
        diagnosticsMessages.add("TEST_METHOD");
      }
      if (MoreJUnitMatchers.HAS_METHOD_SOURCE.matches(tree, state)) {
        diagnosticsMessages.add("HAS_METHOD_SOURCE");
      }
      if (MoreJUnitMatchers.SETUP_OR_TEARDOWN_METHOD.matches(tree, state)) {
        diagnosticsMessages.add("SETUP_OR_TEARDOWN_METHOD");
      }

      return diagnosticsMessages.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree).setMessage(String.join(", ", diagnosticsMessages)).build();
    }
  }
}
