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
            "import java.util.function.Function;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  Integer negative1() {",
            "    // BUG: Diagnostic contains:",
            "    Function<String, Integer> parseIntFunction = (String s) -> Integer.parseInt(s);",
            "    return Stream.of(\"1\")",
            "        .map(parseIntFunction)",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0, Integer::sum);",
            "  }",
            "",
            "  Integer negative2() {",
            "    // BUG: Diagnostic contains:",
            "    Function<String, Integer> stringLengthMethodReference = String::length;",
            "    return Stream.of(\"1\")",
            "        .map(stringLengthMethodReference)",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0, Integer::sum);",
            "  }",
            "",
            "  Double negative3() {",
            "    Function<String, Double> parseDoubleFunction =",
            "        new Function<String, Double>() {",
            "          @Override",
            "          public Double apply(String s) {",
            "            return Double.parseDouble(s);",
            "          }",
            "        };",
            "    return Stream.of(\"1\")",
            "        .map(parseDoubleFunction)",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0.0, Double::sum);",
            "  }",
            "",
            "  Long negative4() {",
            "    class ParseLongFunction implements Function<String, Long> {",
            "      @Override",
            "      public Long apply(String s) {",
            "        return Long.parseLong(s);",
            "      }",
            "    }",
            "    return Stream.of(\"1\")",
            "        .map(new ParseLongFunction())",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0L, Long::sum);",
            "  }",
            "",
            "  Integer positive1() {",
            "    return Stream.of(1)",
            "        // BUG: Diagnostic contains:",
            "        .map(i -> i * 2)",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0, Integer::sum);",
            "  }",
            "",
            "  Long positive2() {",
            "    return Stream.of(1)",
            "        .map(",
            "            // BUG: Diagnostic contains:",
            "            i -> {",
            "              return i * 2L;",
            "            })",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0L, Long::sum);",
            "  }",
            "",
            "  Double positive3() {",
            "    return Stream.of(\"1\")",
            "        // BUG: Diagnostic contains:",
            "        .map(Double::parseDouble)",
            "        // BUG: Diagnostic contains:",
            "        .reduce(0.0, Double::sum);",
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
