package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsNullLiteralTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  String negative1() {",
            "    return \"a\";",
            "  }",
            "",
            "  Object negative2() {",
            "    return (Object) 1;",
            "  }",
            "",
            "  Object positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return null;",
            "  }",
            "",
            "  String positive2() {",
            "    return (String)",
            "        // BUG: Diagnostic contains:",
            "        null;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsNullLiteral}. */
  @BugPattern(summary = "Flags expressions matched by `IsNullLiteral`", severity = ERROR)
  private static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    private MatcherTestChecker() {
      super(new IsNullLiteral());
    }
  }
}
