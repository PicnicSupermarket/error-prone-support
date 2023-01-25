package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tech.picnic.errorprone.documentation.DocumentationGenerator.DOCS_DIRECTORY;

import com.google.common.io.Resources;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternExtractorTest {
  private Path outputPath;

  @BeforeEach
  void setUp(@TempDir Path directory) {
    outputPath = directory.resolve("pkg");
  }

  @Test
  void noJsonExpected() {
    TaskListenerCompiler.compile(
        outputPath,
        "TestCheckerWithoutAnnotation.java",
        "package pkg;",
        "",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputPath.resolve(DOCS_DIRECTORY).toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void minimalBugPattern() throws IOException {
    TaskListenerCompiler.compile(
        outputPath,
        "MinimalBugChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(summary = \"MinimalBugChecker summary\", severity = SeverityLevel.ERROR)",
        "public final class MinimalBugChecker extends BugChecker {}");

    assertThat(readFile("bugpattern-MinimalBugChecker.json"))
        .isEqualToIgnoringWhitespace(getResource("bugpattern-documentation-minimal.json"));
  }

  @Test
  void completeBugPattern() throws IOException {
    TaskListenerCompiler.compile(
        outputPath,
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

    assertThat(readFile("bugpattern-CompleteBugChecker.json"))
        .isEqualToIgnoringWhitespace(getResource("bugpattern-documentation-complete.json"));
  }

  @Test
  void undocumentedSuppressionBugPattern() throws IOException {
    TaskListenerCompiler.compile(
        outputPath,
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

    assertThat(readFile("bugpattern-UndocumentedSuppressionBugPattern.json"))
        .isEqualToIgnoringWhitespace(
            getResource("bugpattern-documentation-undocumented-suppression.json"));
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

  private String readFile(String fileName) throws IOException {
    return Files.readString(outputPath.resolve(DOCS_DIRECTORY).resolve(fileName));
  }

  // XXX: Once we support only JDK 15+, drop this method in favour of including the resources as
  // text blocks in this class.
  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternExtractorTest.class, resourceName), UTF_8);
  }
}
