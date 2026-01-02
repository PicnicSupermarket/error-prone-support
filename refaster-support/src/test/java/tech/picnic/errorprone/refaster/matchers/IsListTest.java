package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsListTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.ArrayDeque;",
            "import java.util.ArrayList;",
            "import java.util.Deque;",
            "import java.util.LinkedList;",
            "import java.util.List;",
            "",
            "class A {",
            "  Object negative1() {",
            "    return new ArrayDeque<String>();",
            "  }",
            "",
            "  Deque<String> negative2() {",
            "    return new ArrayDeque<>();",
            "  }",
            "",
            "  List<String> positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return new ArrayList<String>();",
            "  }",
            "",
            "  List<String> positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return new LinkedList<String>();",
            "  }",
            "",
            "  List<String> positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return List.of();",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsList}. */
  @BugPattern(summary = "Flags expressions matched by `IsList`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsList());
    }
  }
}
