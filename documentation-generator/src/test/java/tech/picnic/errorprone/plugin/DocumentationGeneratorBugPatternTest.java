package tech.picnic.errorprone.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

final class DocumentationGeneratorBugPatternTest extends DocumentationGeneratorTaskListenerTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsWindows() {
    wrongPathFails('?');
  }

  @DisabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsOtherOperatingSystems() {
    // Strictly speaking we are validating here that we cannot write to a Read-only file system.
    wrongPathFails('/');
  }

  private void wrongPathFails(char invalidCharacter) {
    String invalidPath = invalidCharacter + "wrong-path";
    assertThatThrownBy(() -> compile(invalidPath))
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith(
            "Error while creating directory with path '%s'", invalidPath + File.separator + "docs");
  }

  @Test
  void noClass(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "package pkg;");

    assertThat(outputPath.resolve("docs").resolve("bugpattern-TaskListenerTestInput.json").toFile())
        .doesNotExist();
  }

  @Test
  void noJsonExpected(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(
        outputPath.toString(),
        "package pkg;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputPath.resolve("docs").resolve("bugpattern-TaskListenerTestInput.json").toFile())
        .doesNotExist();
  }

  @Test
  void minimalBugPattern(@TempDir Path directory) throws IOException {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(
        outputPath.toString(),
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
                outputPath.resolve("docs").resolve("bugpattern-TaskListenerTestInput.json")))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_minimal_testdata.json"));
  }

  @Test
  void completeBugPattern(@TempDir Path directory) throws IOException {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(
        outputPath.toString(),
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel",
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

    assertThat(
            Files.readString(
                outputPath.resolve("docs").resolve("bugpattern-TaskListenerTestInput.json")))
        .isEqualToIgnoringWhitespace(getResource("bugpattern_example_testdata.json"));
  }

  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(DocumentationGeneratorBugPatternTest.class, resourceName), UTF_8);
  }
}
