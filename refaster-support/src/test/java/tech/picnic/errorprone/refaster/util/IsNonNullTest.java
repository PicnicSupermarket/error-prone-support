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

final class IsNonNullTest {

  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(TestChecker.class, getClass());

  @Test
  public void identification() {
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
            "    id(nullableObj);",
            "    // BUG: Diagnostic contains:",
            "    id(nonnullObj);",
            "    foo(id(nullableObj));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(nonnullObj));",
            "  }",
            "  void literal_tests() {",
            "    foo(id(null));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(this));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(5));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(\"hello\"));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(new Object()));",
            "    // BUG: Diagnostic contains:",
            "    foo(id(new Object[0]));",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} which simply delegates to {@link IsNonNull}. */
  @BugPattern(name = "TestChecker", summary = "Flags non-null expressions", severity = ERROR)
  public static final class TestChecker extends BugChecker implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return new IsNonNull().matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
