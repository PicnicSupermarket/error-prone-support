package tech.picnic.errorprone.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.picnic.errorprone.plugin.DocumentationGenerator.DOCS_DIRECTORY;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternTaskListenerTest extends TaskListenerCompilerBasedTest {
  private Path outputPath;

  @BeforeEach
  void setUp(@TempDir Path directory) {
    outputPath = directory.resolve("pkg");
  }

  @Test
  void noJsonExpected() {
    compile(
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
    compile(
        outputPath.toString(),
        "MinimalTestChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(summary = \"Example summary\", severity = BugPattern.SeverityLevel.ERROR)",
        "public final class MinimalTestChecker extends BugChecker {}");

    assertThat(readFile("bugpattern-MinimalTestChecker.json"))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_minimal_testdata.json"));
  }

  @Test
  void completeBugPattern() throws IOException {
    compile(
        outputPath.toString(),
        "TestChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(",
        "    name = \"OtherName\",",
        "    summary = \"Example summary\",",
        "    linkType = BugPattern.LinkType.CUSTOM,",
        "    link = \"https://error-prone.picnic.tech\",",
        "    explanation = \"Example explanation\",",
        "    severity = SeverityLevel.SUGGESTION,",
        "    altNames = \"Check\",",
        "    tags = BugPattern.StandardTags.SIMPLIFICATION,",
        "    disableable = false)",
        "public final class TestChecker extends BugChecker {}");

    assertThat(readFile("bugpattern-TestChecker.json"))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_testdata.json"));
  }

  private String readFile(String fileName) throws IOException {
    return Files.readString(outputPath.resolve(DOCS_DIRECTORY).resolve(fileName));
  }

  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternTaskListenerTest.class, resourceName), UTF_8);
  }
}
