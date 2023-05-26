package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import com.sun.source.tree.ReturnTree;
import org.junit.jupiter.api.Test;

final class IsLikelyTrivialComputationTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Predicate;",
            "",
            "class A {",
            "  String negative1() {",
            "    return String.valueOf(1);",
            "  }",
            "",
            "  String negative2() {",
            "    return toString().toString();",
            "  }",
            "",
            "  String negative3() {",
            "    return \"foo\" + toString();",
            "  }",
            "",
            "  byte negative4() {",
            "    return \"foo\".getBytes()[0];",
            "  }",
            "",
            "  int negative5() {",
            "    int[] arr = new int[0];",
            "    return arr[hashCode()];",
            "  }",
            "",
            "  int negative6() {",
            "    return 1 * 2;",
            "  }",
            "",
            "  Predicate<String> negative7() {",
            "    return toString()::equals;",
            "  }",
            "",
            "  String negative8() {",
            "    return (toString());",
            "  }",
            "",
            "  Object negative9() {",
            "    return (Object) toString();",
            "  }",
            "",
            "  int negative10() {",
            "    return -hashCode();",
            "  }",
            "",
            "  String positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return toString();",
            "  }",
            "",
            "  String positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return this.toString();",
            "  }",
            "",
            "  int positive3() {",
            "    int[] arr = new int[0];",
            "    // BUG: Diagnostic contains:",
            "    return arr[0];",
            "  }",
            "",
            "  String positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return null;",
            "  }",
            "",
            "  boolean positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return false;",
            "  }",
            "",
            "  int positive6() {",
            "    // BUG: Diagnostic contains:",
            "    return 0;",
            "  }",
            "",
            "  String positive7() {",
            "    // BUG: Diagnostic contains:",
            "    return \"foo\" + \"bar\";",
            "  }",
            "",
            "  Predicate<String> positive8() {",
            "    // BUG: Diagnostic contains:",
            "    return v -> \"foo\".equals(v);",
            "  }",
            "",
            "  A positive9() {",
            "    // BUG: Diagnostic contains:",
            "    return this;",
            "  }",
            "",
            "  Predicate<String> positive10() {",
            "    // BUG: Diagnostic contains:",
            "    return \"foo\"::equals;",
            "  }",
            "",
            "  A positive11() {",
            "    // BUG: Diagnostic contains:",
            "    return (this);",
            "  }",
            "",
            "  Object positive12() {",
            "    // BUG: Diagnostic contains:",
            "    return (Object) this;",
            "  }",
            "",
            "  boolean positive13() {",
            "    // BUG: Diagnostic contains:",
            "    return !false;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsLikelyTrivialComputation}. */
  @BugPattern(
      summary = "Flags return statement expressions matched by `IsLikelyTrivialComputation`",
      severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(
          (expressionTree, state) ->
              state.getPath().getParentPath().getLeaf() instanceof ReturnTree
                  && new IsLikelyTrivialComputation().matches(expressionTree, state));
    }
  }
}
