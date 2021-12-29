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

final class IsIdentityTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static java.util.function.Function.identity;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.Streams;",
            "",
            "class A {",
            "  void positive() {",
            "    // BUG: Diagnostic contains:",
            "    java.util.function.Function.identity();",
            "    // BUG: Diagnostic contains:",
            "    com.google.common.base.Functions.identity();",
            "  }",
            "",
            "  public static void identity() { }",
            "",
            "  static void negative() {",
            "    identity();",
            "    A.identity();",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} which simply delegates to {@link IsIdentity}. */
  @BugPattern(name = "TestChecker", summary = "Flags identity method invocations", severity = ERROR)
  public static final class TestChecker extends BugChecker implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return new IsIdentity().matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
