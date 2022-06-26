package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodInvocationTree;
import org.junit.jupiter.api.Test;

final class IsArrayTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  Object negative1() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  String negative2() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  int negative3() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  Object[] positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return alwaysNull();",
            "  }",
            "",
            "  String[] positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return alwaysNull();",
            "  }",
            "",
            "  int[] positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return alwaysNull();",
            "  }",
            "",
            "  private static <T> T alwaysNull() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} which simply delegates to {@link IsArray}. */
  @BugPattern(
      name = "TestChecker",
      summary = "Flags array-returning method invocations",
      severity = ERROR)
  public static final class TestChecker extends BugChecker implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return new IsArray().matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
