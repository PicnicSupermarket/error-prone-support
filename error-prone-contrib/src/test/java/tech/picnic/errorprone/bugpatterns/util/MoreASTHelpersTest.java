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
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

final class MoreASTHelpersTest {
  @Test
  void findMethods() {
    CompilationTestHelper.newInstance(FindMethodsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains: foo: 1, bar: 2, baz: 0",
            "  void foo() {}",
            "",
            "  // BUG: Diagnostic contains: foo: 1, bar: 2, baz: 0",
            "  void bar() {}",
            "",
            "  // BUG: Diagnostic contains: foo: 1, bar: 2, baz: 0",
            "  void bar(int i) {}",
            "",
            "  static class B {",
            "    // BUG: Diagnostic contains: foo: 0, bar: 1, baz: 1",
            "    void bar() {}",
            "",
            "    // BUG: Diagnostic contains: foo: 0, bar: 1, baz: 1",
            "    void baz() {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodExistsInEnclosingClass() {
    CompilationTestHelper.newInstance(MethodExistsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains: foo: true, bar: true, baz: false",
            "  void foo() {}",
            "",
            "  // BUG: Diagnostic contains: foo: true, bar: true, baz: false",
            "  void bar() {}",
            "",
            "  // BUG: Diagnostic contains: foo: true, bar: true, baz: false",
            "  void bar(int i) {}",
            "",
            "  static class B {",
            "    // BUG: Diagnostic contains: foo: false, bar: true, baz: true",
            "    void bar() {}",
            "",
            "    // BUG: Diagnostic contains: foo: false, bar: true, baz: true",
            "    void baz() {}",
            "  }",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that delegates to {@link MoreASTHelpers#findMethods(CharSequence,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  public static final class FindMethodsTestChecker extends BugChecker implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              createDiagnosticsMessage(
                  (methodName, s) -> MoreASTHelpers.findMethods(methodName, s).size(), state))
          .build();
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#methodExistsInEnclosingClass(CharSequence, VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  public static final class MethodExistsTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(createDiagnosticsMessage(MoreASTHelpers::methodExistsInEnclosingClass, state))
          .build();
    }
  }

  private static String createDiagnosticsMessage(
      BiFunction<String, VisitorState, Object> function, VisitorState state) {
    return ImmutableSet.of("foo", "bar", "baz").stream()
        .map(
            methodName ->
                String.join(": ", methodName, String.valueOf(function.apply(methodName, state))))
        .collect(joining(", "));
  }
}
