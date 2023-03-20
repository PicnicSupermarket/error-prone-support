package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternExtractorTest {
  @Test
  void noBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void minimalBugPattern(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MinimalBugChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(summary = \"MinimalBugChecker summary\", severity = SeverityLevel.ERROR)",
        "public final class MinimalBugChecker extends BugChecker {}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-MinimalBugChecker.json",
        "bugpattern-documentation-minimal.json");
  }

  @Test
  void completeBugPattern(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompleteBugChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import org.junit.jupiter.api.Test;",
        "",
        "@BugPattern(",
        "    name = \"OtherName\",",
        "    summary = \"CompleteBugChecker summary\",",
        "    linkType = BugPattern.LinkType.CUSTOM,",
        "    link = \"https://error-prone.picnic.tech\",",
        "    explanation = \"Example explanation\",",
        "    severity = SeverityLevel.SUGGESTION,",
        "    altNames = \"Check\",",
        "    tags = BugPattern.StandardTags.SIMPLIFICATION,",
        "    disableable = false,",
        "    suppressionAnnotations = {BugPattern.class, Test.class})",
        "public final class CompleteBugChecker extends BugChecker {}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-CompleteBugChecker.json",
        "bugpattern-documentation-complete.json");
  }

  @Test
  void undocumentedSuppressionBugPattern(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "UndocumentedSuppressionBugPattern.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(",
        "    summary = \"UndocumentedSuppressionBugPattern summary\",",
        "    severity = SeverityLevel.WARNING,",
        "    documentSuppression = false)",
        "public final class UndocumentedSuppressionBugPattern extends BugChecker {}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-UndocumentedSuppressionBugPattern.json",
        "bugpattern-documentation-undocumented-suppression.json");
  }

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

  private static void verifyFileMatchesResource(
      Path outputDirectory, String fileName, String resourceName) throws IOException {
    assertThat(outputDirectory.resolve(fileName))
        .content(UTF_8)
        .isEqualToIgnoringWhitespace(getResource(resourceName));
  }

  // XXX: Once we support only JDK 15+, drop this method in favour of including the resources as
  // text blocks in this class. (This also requires renaming the `verifyFileMatchesResource`
  // method.)
  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternExtractorTest.class, resourceName), UTF_8);
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
