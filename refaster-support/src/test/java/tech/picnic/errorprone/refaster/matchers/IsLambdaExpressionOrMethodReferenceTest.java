package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class IsLambdaExpressionOrMethodReferenceTest {

  @Disabled
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Function;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  Integer negative1() {",
            "    Function<String, Integer> parseIntFunction = (String s) -> Integer.parseInt(s);",
            "    return Stream.of(\"1\").map(parseIntFunction).reduce(0, Integer::sum);",
            "  }",
            "",
            "  Integer negative2() {",
            "    Function<String, Integer> stringLengthMethodReference = String::length;",
            "    return Stream.of(\"1\").map(stringLengthMethodReference).reduce(0, Integer::sum);",
            "  }",
            "",
            "  Double negative3() {",
            "    Function<String, Double> parseDoubleFunction = new Function<String, Double>() {",
            "      @Override",
            "      public Double apply(String s) {",
            "        return Double.parseDouble(s);",
            "      }",
            "    };",
            "    return Stream.of(\"1\").map(parseDoubleFunction).reduce(0.0, Double::sum);",
            "  }",
            "",
            "  Long negative4() {",
            "    class ParseLongFunction implements Function<String, Long> {",
            "      @Override",
            "      public Long apply(String s) {",
            "        return Long.parseLong(s);",
            "      }",
            "    }",
            "    return Stream.of(\"1\").map(new ParseLongFunction()).reduce(0L, Long::sum);",
            "  }",
            "",
            "  Integer positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return Stream.of(1).map(i -> i * 2).reduce(0, Integer::sum);",
            "  }",
            "",
            "  Long positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return Stream.of(1)",
            "    .map(",
            "      i -> {",
            "        return i * 2L;",
            "    })",
            "    .reduce(0L, Long::sum);",
            "  }",
            "",
            "  Double positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return Stream.of(\"1\").map(Double::parseDouble).reduce(0.0, Double::sum);",
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

    public MatcherTestChecker() {
      super(new IsLambdaExpressionOrMethodReference());
    }
  }
}
