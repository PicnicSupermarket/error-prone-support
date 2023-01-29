package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol;
import org.junit.jupiter.api.Test;

final class ConflictDetectionTest {
  @Test
  void matcher() {
    CompilationTestHelper.newInstance(RenameBlockerFlagger.class, getClass())
        .addSourceLines(
            "/A.java",
            "import static pkg.B.foo3t;",
            "",
            "class A {",
            "  private void foo1() {",
            "    foo3t();",
            "  }",
            "",
            "  // BUG: Diagnostic contains: a method named `foo2t` is already defined in this class or a",
            "  // supertype",
            "  private void foo2() {}",
            "",
            "  private void foo2t() {}",
            "",
            "  // BUG: Diagnostic contains: `foo3t` is already statically imported",
            "  private void foo3() {}",
            "",
            "  // BUG: Diagnostic contains: `int` is not a valid identifier",
            "  private void in() {}",
            "}")
        .addSourceLines(
            "/pkg/B.java",
            "package pkg;",
            "",
            "public class B {",
            "  public static void foo3t() {}",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that flags method rename blockers found by {@link
   * ConflictDetection#findMethodRenameBlocker(Symbol.MethodSymbol, String, VisitorState)}.
   */
  @BugPattern(summary = "Flags blockers for renaming methods", severity = ERROR)
  public static final class RenameBlockerFlagger extends BugChecker implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return ConflictDetection.findMethodRenameBlocker(
              ASTHelpers.getSymbol(tree), tree.getName() + "t", state)
          .map(blocker -> buildDescription(tree).setMessage(blocker).build())
          .orElse(Description.NO_MATCH);
    }
  }
}
