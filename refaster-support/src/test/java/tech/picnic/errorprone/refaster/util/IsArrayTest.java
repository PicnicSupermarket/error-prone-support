package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsArrayTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
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

  /** A {@link BugChecker} that simply delegates to {@link IsArray}. */
  @BugPattern(summary = "Flags expressions matched by `IsArray`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsArray());
    }
  }
}
