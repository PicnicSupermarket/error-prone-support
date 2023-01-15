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
import org.junit.jupiter.api.Test;

final class BugPatternExtractorTest {
  @Test
  void bugPatternAnnotationIsAbsent() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "TestChecker.java",
            "import com.google.errorprone.bugpatterns.BugChecker;",
            "",
            "// BUG: Diagnostic contains: Can extract: false",
            "public final class TestChecker extends BugChecker {}")
        .doTest();
  }

  /** A {@link BugChecker} that validates the {@link BugPatternExtractor}. */
  @BugPattern(summary = "Validates `BugPatternExtractor` extraction", severity = ERROR)
  public static final class TestChecker extends BugChecker implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      BugPatternExtractor extractor = new BugPatternExtractor();

      assertThatThrownBy(() -> extractor.extract(tree, state.context))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("BugPattern annotation must be present");

      return buildDescription(tree)
          .setMessage(String.format("Can extract: %s", extractor.canExtract(tree)))
          .build();
    }
  }
}
