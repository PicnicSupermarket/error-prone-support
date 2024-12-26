package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsIdentityOperationTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.BinaryOperator;",
            "import java.util.function.DoubleUnaryOperator;",
            "import java.util.function.Function;",
            "import java.util.function.IntUnaryOperator;",
            "import java.util.function.LongUnaryOperator;",
            "import java.util.function.UnaryOperator;",
            "",
            "class A {",
            "  BinaryOperator<String> negative1() {",
            "    return (a, b) -> a;",
            "  }",
            "",
            "  UnaryOperator<String> negative2() {",
            "    return a -> a + a;",
            "  }",
            "",
            "  DoubleUnaryOperator positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return DoubleUnaryOperator.identity();",
            "  }",
            "",
            "  Function<Integer, Integer> positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return Function.identity();",
            "  }",
            "",
            "  UnaryOperator<String> positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return UnaryOperator.identity();",
            "  }",
            "",
            "  IntUnaryOperator positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return IntUnaryOperator.identity();",
            "  }",
            "",
            "  LongUnaryOperator positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return LongUnaryOperator.identity();",
            "  }",
            "",
            "  UnaryOperator positive6() {",
            "    // BUG: Diagnostic contains:",
            "    return a -> a;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsIdentityOperation}. */
  @BugPattern(summary = "Flags expressions matched by `IsIdentityOperation`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsIdentityOperation());
    }
  }
}
