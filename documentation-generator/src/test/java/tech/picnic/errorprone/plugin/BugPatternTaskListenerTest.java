package tech.picnic.errorprone.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternTaskListenerTest extends TaskListenerCompilerBasedTest {
  @Test
  void noJsonExpected(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(
        outputPath.toString(),
        "TestCheckerWithoutAnnotation.java",
        "package pkg;",
        "",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    Path docsPath = outputPath.resolve("docs").toAbsolutePath();
    assertThat(docsPath).isEmptyDirectory();
  }

  @Test
  void minimalBugPattern(@TempDir Path directory) throws IOException {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
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

    assertThat(
            Files.readString(
                outputPath.resolve("docs").resolve("bugpattern-MinimalTestChecker.json")))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_minimal_testdata.json"));
  }

  @Test
  void completeBugPattern(@TempDir Path directory) throws IOException {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
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

    assertThat(Files.readString(outputPath.resolve("docs").resolve("bugpattern-TestChecker.json")))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_testdata.json"));
  }

  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternTaskListenerTest.class, resourceName), UTF_8);
  }
}
