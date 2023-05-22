package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class HasTypeArgumentsTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "import java.util.List;",
            "",
            "class A {",
            "  Object negative1() {",
            "    return alwaysNull();",
            "  }",
            "",
            "  Object negative2() {",
            "    return new Object();",
            "  }",
            "",
            "  List<Integer> negative3() {",
            "    return new ArrayList<>();",
            "  }",
            "",
            "  <E> ImmutableSet<E> positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return ImmutableSet.<E>builder().build();",
            "  }",
            "",
            "  <E> ImmutableSet<E> positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return new ImmutableSet.Builder<E>().build();",
            "  }",
            "",
            "  private static <T> T alwaysNull() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link HasTypeArguments}. */
  @BugPattern(summary = "Flags expressions matched by `HasTypeArguments`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues2/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new HasTypeArguments());
    }
  }
}
