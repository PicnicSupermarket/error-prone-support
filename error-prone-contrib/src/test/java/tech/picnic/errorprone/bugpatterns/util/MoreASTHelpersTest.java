package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodTree;
import org.junit.jupiter.api.Test;

final class MoreASTHelpersTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "/A.java",
            "class A {",
            "  // BUG: Diagnostic contains: foo: (1, true), bar: (2, true), baz: (0, false)",
            "  void foo() {}",
            "",
            "  // BUG: Diagnostic contains: (1, true), bar: (2, true), baz: (0, false)",
            "  void bar() {}",
            "",
            "  // BUG: Diagnostic contains: foo: (1, true), bar: (2, true), baz: (0, false)",
            "  void bar(int i) {}",
            "",
            "  static class B {",
            "    // BUG: Diagnostic contains: (0, false), bar: (1, true), baz: (1, true)",
            "    void bar() {}",
            "",
            "    // BUG: Diagnostic contains: (0, false), bar: (1, true), baz: (1, true)",
            "    void baz() {}",
            "  }",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that flags methods with a diagnostics message that indicates, for each
   * method, the result of calling the methods from {@link MoreASTHelpers}.
   */
  @BugPattern(
      summary = "Interacts with `MoreASTHelpersTest` for testing purposes",
      severity = ERROR)
  public static final class TestChecker extends BugChecker implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    private static final ImmutableSet<String> METHOD_NAMES = ImmutableSet.of("foo", "bar", "baz");

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              METHOD_NAMES.stream()
                  .map(methodName -> String.join(": ", methodName, collectData(methodName, state)))
                  .collect(joining(", ")))
          .build();
    }

    private static String collectData(String methodName, VisitorState state) {
      return String.format(
          "(%s, %s)",
          MoreASTHelpers.findMethods(methodName, state).size(),
          MoreASTHelpers.isMethodInEnclosingClass(methodName, state));
    }
  }
}
