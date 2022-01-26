package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import org.junit.jupiter.api.Test;

final class IsNullableTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(TestChecker.class, getClass());

  @Test
  void identification() {
    compilationHelper
        .addSourceLines(
            "IdentityTest.java",
            "package pkg;",
            "import org.checkerframework.checker.nullness.qual.Nullable;",
            "import org.checkerframework.checker.nullness.qual.NonNull;",
            "public class IdentityTest {",
            "",
            "  public static <T> T foo(T t) {",
            "    return t;",
            "  }",
            "",
            "  @Nullable Object nullableObj;",
            "  @NonNull Object nonnullObj;",
            "  <T> T id(T t) { return t; }",
            "  void id_tests() {",
            "    // BUG: Diagnostic contains:",
            "    id(nullableObj);",
            "    id(nonnullObj);",
            "    // BUG: Diagnostic contains:",
            "    foo(id(nullableObj));",
            "    foo(id(nonnullObj));",
            "  }",
            "  void literal_tests() {",
            "    foo(id(null));",
            "    foo(id(this));",
            "    foo(id(5));",
            "    foo(id(\"hello\"));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(nullableObj));",
            "    foo(id(nonnullObj));",
            "  }",
            "  private void m(@Nullable String nullableString) {",
            "    // BUG: Diagnostic contains:",
            "    String s = nullableString;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} which simply delegates to {@link IsNullable}. */
  @BugPattern(name = "TestChecker", summary = "Flags non-null expressions", severity = ERROR)
  public static final class TestChecker extends BugChecker
      implements BugChecker.VariableTreeMatcher, MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return new IsNullable().matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }

    @Override
    public Description matchVariable(VariableTree tree, VisitorState state) {
      if (tree.getInitializer() == null) {
        return Description.NO_MATCH;
      }
      return new IsNullable().matches(tree.getInitializer(), state)
          ? describeMatch(tree)
          : Description.NO_MATCH;
    }
  }
}
