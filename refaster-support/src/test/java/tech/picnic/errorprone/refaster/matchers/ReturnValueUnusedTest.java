package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class ReturnValueUnusedTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Consumer;",
            "",
            "class A {",
            "  String negative1() {",
            "    return toString();",
            "  }",
            "",
            "  void negative2() {",
            "    String s = toString();",
            "  }",
            "",
            "  String negative3() {",
            "    return toString().toString();",
            "  }",
            "",
            // XXX: The `valueOf` result is ignored, but `String::valueOf` itself isn't. Review.
            "  Object negative4() {",
            "    return sink(String::valueOf);",
            "  }",
            "",
            "  void positive1() {",
            "    // BUG: Diagnostic contains:",
            "    toString();",
            "  }",
            "",
            "  void positive2() {",
            "    // BUG: Diagnostic contains:",
            "    toString().toString();",
            "  }",
            "",
            "  void positive3() {",
            "    // BUG: Diagnostic contains:",
            "    new Object();",
            "  }",
            "",
            "  Object positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return sink(v -> toString());",
            "  }",
            "",
            "  Object positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return sink(v -> new Object());",
            "  }",
            "",
            "  private <T, S> S sink(Consumer<T> consumer) {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link ReturnValueUnused}. */
  @BugPattern(summary = "Flags expressions matched by `ReturnValueUnused`", severity = ERROR)
  @SuppressWarnings({"RedundantModifier", "serial"})
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new ReturnValueUnused());
    }
  }
}
