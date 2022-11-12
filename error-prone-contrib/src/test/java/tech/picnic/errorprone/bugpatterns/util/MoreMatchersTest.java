package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import org.junit.jupiter.api.Test;

final class MoreMatchersTest {
  @Test
  void matcher() {
    CompilationTestHelper.newInstance(TestMatcher.class, getClass())
        .addSourceLines(
            "/A.java",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.api.TestTemplate;",
            "",
            "class A {",
            "  private void negative1() {}",
            "",
            "  @Test",
            "  void negative2() {}",
            "",
            "",
            "  @TestTemplate",
            "  void negative3() {}",
            "",
            "  @AfterAll",
            "  void negative4() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest",
            "  void testBar() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RepeatedTest(2)",
            "  void testBaz() {}",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that delegates to `MoreMatchers#hasMetaAnnotation`. */
  @BugPattern(summary = "Interacts with `MoreMatchers` for testing purposes", severity = ERROR)
  public static final class TestMatcher extends BugChecker implements AnnotationTreeMatcher {
    private static final long serialVersionUID = 1L;

    private static final Matcher<AnnotationTree> DELEGATE =
        MoreMatchers.hasMetaAnnotation("org.junit.jupiter.api.TestTemplate");

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? buildDescription(tree).build() : Description.NO_MATCH;
    }
  }
}
