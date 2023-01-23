package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.picnic.errorprone.documentation.DocumentationGenerator.DOCS_DIRECTORY;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// XXX: Even though this class _also_ exercises `DocumentationGenerator` and
// `DocumentationGeneratorTaskListener`, move these tests to `BugPatternExtractorTest`.
final class BugPatternTaskListenerTest {
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

  private String readFile(String fileName) throws IOException {
    return Files.readString(outputPath.resolve(DOCS_DIRECTORY).resolve(fileName));
  }

  // XXX: Once we support only JDK 15+, drop this method in favour of including the resources as
  // text blocks in this class.
  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternTaskListenerTest.class, resourceName), UTF_8);
  }
}
