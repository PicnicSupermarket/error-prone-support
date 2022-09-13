package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsCharacterTest {
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
            "  String negative2() {",
            "    return \"a\".toCharArray().toString();",
            "  }",
            "",
            "  Character positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return 'a';",
            "  }",
            "",
            "  Character positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return Character.valueOf('a');",
            "  }",
            "",
            "  char positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return \"a\".charAt(1);",
            "  }",
            "",
            "  char positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return Character.valueOf((char) 1);",
            "  }",
            "",
            "  char positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return (char) 1;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} which simply delegates to {@link IsCharacter}. */
  @BugPattern(summary = "Flags expressions matched by `IsCharacter`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsCharacter());
    }
  }
}
