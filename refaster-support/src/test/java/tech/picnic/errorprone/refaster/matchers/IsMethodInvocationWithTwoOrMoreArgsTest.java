package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsMethodInvocationWithTwoOrMoreArgsTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  Boolean negative1() {",
            "    return Boolean.TRUE;",
            "  }",
            "",
            "  String negative2() {",
            "    return \"foo\";",
            "  }",
            "",
            "  String negative3() {",
            "    return toString();",
            "  }",
            "",
            "  String negative4() {",
            "    return String.valueOf(1);",
            "  }",
            "",
            "  String positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return m1(\"foo\", \"bar\");",
            "  }",
            "",
            "  String positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return String.format(\"%s\", \"foo\");",
            "  }",
            "",
            "  String positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return String.format(\"%s-%s\", \"foo\", \"bar\");",
            "  }",
            "",
            "  private static String m1(String foo, String bar) {",
            "    return foo + bar;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsMethodInvocationWithTwoOrMoreArgs}. */
  @BugPattern(
      summary = "Flags expressions matched by `IsMethodInvocationWithTwoOrMoreArgs`",
      severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsMethodInvocationWithTwoOrMoreArgs());
    }
  }
}
