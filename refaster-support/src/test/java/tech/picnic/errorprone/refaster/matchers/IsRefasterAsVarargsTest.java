package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsRefasterAsVarargsTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.errorprone.refaster.Refaster;

            class A {
              int[] negative1() {
                return new int[4];
              }

              String[] negative2() {
                return "foo".split("o");
              }

              String[] negative3() {
                return asVarArgs("bar");
              }

              String[] positive1() {
                // BUG: Diagnostic contains:
                return Refaster.asVarargs("o");
              }

              private static String[] asVarArgs(String s) {
                return s.split("a");
              }
            }
            """)
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsRefasterAsVarargs}. */
  @BugPattern(summary = "Flags expressions matched by `IsRefasterAsVarargs`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsRefasterAsVarargs());
    }
  }
}
