package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsMethodInvocationWithTwoOrMoreArgsTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(
            IsMethodInvocationWithTwoOrMoreArgsTest.MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  String foo(String foo, String bar) {",
            "    return foo + bar;",
            "  }",
            "",
            "  String foo(String foo) {",
            "    return foo;",
            "  }",
            "",
            "  String foo() {",
            "    return \"foo\";",
            "  }",
            "",
            "  Boolean negative1() {",
            "    return Boolean.TRUE;",
            "  }",
            "",
            "  String negative2() {",
            "    return \"foo\";",
            "  }",
            "",
            "  String negative3() {",
            "    return foo(\"foo\");",
            "  }",
            "",
            "  String negative4() {",
            "    return foo();",
            "  }",
            "",
            "  String positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return foo(\"foo\", \"bar\");",
            "  }",
            "",
            "  String positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return String.format(\"%s\", \"foo\");",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsCharacter}. */
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
