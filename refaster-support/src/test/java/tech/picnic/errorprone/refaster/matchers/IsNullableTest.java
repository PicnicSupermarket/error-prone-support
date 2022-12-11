package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsNullableTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.Map;",
            "",
            "class A {",
            "  String negative1() {",
            "    return \"a\";",
            "  }",
            "",
            "  String negative2() {",
            "    return negative1();",
            "  }",
            "",
            "  char[] negative3() {",
            "    return \"a\".toCharArray();",
            "  }",
            "",
            "  byte negative4() {",
            "    return (byte) 0;",
            "  }",
            "",
            "  Integer negative5(Integer param) {",
            "    return param;",
            "  }",
            "",
            "  Character positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return null;",
            "  }",
            "",
            "  Character positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return positive1();",
            "  }",
            "",
            "  Integer positive3(Map<String, Integer> map) {",
            "    // BUG: Diagnostic contains:",
            "    return map.get(\"foo\");",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsNullable}. */
  @BugPattern(summary = "Flags expressions matched by `IsNullable`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsNullable());
    }
  }
}
