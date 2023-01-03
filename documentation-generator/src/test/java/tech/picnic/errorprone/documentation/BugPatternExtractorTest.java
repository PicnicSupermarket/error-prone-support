package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import org.junit.jupiter.api.Test;

final class BugPatternExtractorTest {
  @Test
  void bugPatternAnnotationIsPresent() {
    CompilationTestHelper.newInstance(ExtractBugPatternDataTestChecker.class, getClass())
        .addSourceLines(
            "pkg/TestChecker.java",
            "package pkg;",
            "",
            "import com.google.errorprone.bugpatterns.BugChecker;",
            "",
            "public final class TestChecker extends BugChecker {}")
        .doTest();
  }

  /** A {@link BugChecker} that validates the {@link BugPatternExtractor}. */
  @BugPattern(summary = "Validates `BugPatternExtractor` extraction", severity = ERROR)
  public static final class ExtractBugPatternDataTestChecker extends BugChecker
      implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      TaskEvent event = new TaskEvent(Kind.ANALYZE);
      BugPatternExtractor extractor = new BugPatternExtractor();

      assertThatThrownBy(() -> extractor.extract(tree, event))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("BugPattern annotation must be present");

      return Description.NO_MATCH;
    }
  }
}
