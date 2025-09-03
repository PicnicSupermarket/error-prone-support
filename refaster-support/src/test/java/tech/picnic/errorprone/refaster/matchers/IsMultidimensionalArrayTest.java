package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsMultidimensionalArrayTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  String negative1() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  int negative2() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  String[] negative3() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  int[] negative4() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  String[][] positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return alwaysNull();",
            "  }",
            "",
            "  int[][][] positive2() {",
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

  /** A {@link BugChecker} that simply delegates to {@link IsMultidimensionalArray}. */
  @BugPattern(summary = "Flags expressions matched by `IsMultidimensionalArray`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsMultidimensionalArray());
    }
  }
}
