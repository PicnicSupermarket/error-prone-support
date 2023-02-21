package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsLambdaExpressionOrMethodReferenceTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.base.Predicates;",
            "import java.util.function.Function;",
            "import java.util.function.Predicate;",
            "",
            "class A {",
            "  boolean negative1() {",
            "    return true;",
            "  }",
            "",
            "  String negative2() {",
            "    return new String(new byte[0]);",
            "  }",
            "",
            "  Predicate<String> negative3() {",
            "    return Predicates.alwaysTrue();",
            "  }",
            "",
            "  Predicate<String> positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return str -> true;",
            "  }",
            "",
            "  Predicate<String> positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return str -> {",
            "      return true;",
            "    };",
            "  }",
            "",
            "  Predicate<String> positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return String::isEmpty;",
            "  }",
            "",
            "  Function<byte[], String> positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return String::new;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsLambdaExpressionOrMethodReference}. */
  @BugPattern(
      summary = "Flags expressions matched by `IsLambdaExpressionOrMethodReference`",
      severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsLambdaExpressionOrMethodReference());
    }
  }
}
