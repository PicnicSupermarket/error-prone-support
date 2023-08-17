package tech.picnic.errorprone.refaster;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import org.junit.jupiter.api.Test;

final class ErrorProneForkTest {
  @Test
  void isSuggestionsAsWarningsEnabledWithoutFlag() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: Suggestions as warnings enabled: false",
            "class A {}")
        .doTest();
  }

  @Test
  void isSuggestionsAsWarningsEnabledWithFlag() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .setArgs("-XepAllSuggestionsAsWarnings")
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: Suggestions as warnings enabled: true",
            "class A {}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that reports the result of {@link
   * ErrorProneFork#isSuggestionsAsWarningsEnabled(ErrorProneOptions)}.
   */
  @BugPattern(summary = "Flags classes with a custom error message", severity = ERROR)
  public static final class TestChecker extends BugChecker implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              String.format(
                  "Suggestions as warnings enabled: %s",
                  ErrorProneFork.isSuggestionsAsWarningsEnabled(state.errorProneOptions())))
          .build();
    }
  }
}
