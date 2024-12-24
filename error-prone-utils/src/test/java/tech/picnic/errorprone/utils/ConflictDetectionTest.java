package tech.picnic.errorprone.utils;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import org.junit.jupiter.api.Test;

final class ConflictDetectionTest {
  @Test
  void matcher() {
    CompilationTestHelper.newInstance(RenameBlockerFlagger.class, getClass())
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import static pkg.A.StaticType.method3t;",
            "import static pkg.A.StaticType.method4t;",
            "",
            "import pkg.A.method4t;",
            "",
            "class A {",
            "  void method1() {",
            "    method4(method4t.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains: a method named `method2t` is already defined in this class or a",
            "  // supertype",
            "  void method2() {}",
            "",
            "  void method2t() {}",
            "",
            "  // BUG: Diagnostic contains: another method named `method3t` is in scope",
            "  void method3() {}",
            "",
            "  void method4(Object o) {}",
            "",
            "  // BUG: Diagnostic contains: `int` is not a valid identifier",
            "  void in() {}",
            "",
            "  class InstanceType {",
            "    void m() {",
            "      System.out.println(method3t());",
            "    }",
            "  }",
            "",
            "  static class StaticType {",
            "    static int method3t() {",
            "      return 0;",
            "    }",
            "",
            "    static void method4t() {",
            "      method4t();",
            "    }",
            "  }",
            "",
            "  record RecordType() {",
            "    void m() {",
            "      method4t();",
            "    }",
            "  }",
            "",
            "  enum EnumType {",
            "    ELEM;",
            "",
            "    void m() {",
            "      method4t();",
            "    }",
            "  }",
            "",
            "  class method4t {}",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that uses {@link ConflictDetection#findMethodRenameBlocker(MethodSymbol,
   * String, VisitorState)} to flag methods of which the name cannot be suffixed with a {@code t}.
   */
  @BugPattern(summary = "Interacts with `ConflictDetection` for testing purposes", severity = ERROR)
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
