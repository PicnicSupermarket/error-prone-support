package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import com.sun.source.tree.ReturnTree;
import org.junit.jupiter.api.Test;

final class RequiresComputationTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Predicate;",
            "",
            "class A {",
            "  int negative1() {",
            "    int[] arr = new int[0];",
            "    return arr[0];",
            "  }",
            "",
            "  String negative2() {",
            "    return null;",
            "  }",
            "",
            "  boolean negative3() {",
            "    return false;",
            "  }",
            "",
            "  int negative4() {",
            "    return 0;",
            "  }",
            "",
            "  String negative5() {",
            "    return \"foo\" + \"bar\";",
            "  }",
            "",
            "  Predicate<String> negative6() {",
            "    return v -> \"foo\".equals(v);",
            "  }",
            "",
            "  A negative7() {",
            "    return this;",
            "  }",
            "",
            "  Predicate<String> negative8() {",
            "    return \"foo\"::equals;",
            "  }",
            "",
            "  A negative9() {",
            "    return (this);",
            "  }",
            "",
            "  Object negative10() {",
            "    return (Object) this;",
            "  }",
            "",
            "  boolean negative11() {",
            "    return !false;",
            "  }",
            "",
            "  String negative12() {",
            "    return \"foo\" + 0;",
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
            "  String positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return String.valueOf(1);",
            "  }",
            "",
            "  String positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return toString().toString();",
            "  }",
            "",
            "  String positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return \"foo\" + toString();",
            "  }",
            "",
            "  byte positive6() {",
            "    // BUG: Diagnostic contains:",
            "    return \"foo\".getBytes()[0];",
            "  }",
            "",
            "  int positive7() {",
            "    int[] arr = new int[0];",
            "    // BUG: Diagnostic contains:",
            "    return arr[hashCode()];",
            "  }",
            "",
            "  Predicate<String> positive8() {",
            "    // BUG: Diagnostic contains:",
            "    return toString()::equals;",
            "  }",
            "",
            "  String positive9() {",
            "    // BUG: Diagnostic contains:",
            "    return (toString());",
            "  }",
            "",
            "  Object positive10() {",
            "    // BUG: Diagnostic contains:",
            "    return (Object) toString();",
            "  }",
            "",
            "  int positive11() {",
            "    // BUG: Diagnostic contains:",
            "    return -hashCode();",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link RequiresComputation}. */
  @BugPattern(
      summary = "Flags return statement expressions matched by `RequiresComputation`",
      severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false negative reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(
          (expressionTree, state) ->
              state.getPath().getParentPath().getLeaf() instanceof ReturnTree
                  && new RequiresComputation().matches(expressionTree, state));
    }
  }
}
