package tech.picnic.errorprone.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.errorprone.FileObjects;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

final class DocumentationGeneratorBugPatternTest extends DocumentationGeneratorCompilerBasedTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsWindows() {
    wrongPathFails('?');
  }

  @DisabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsOtherOperatingSystems() {
    // Strictly speaking we are validating here that we cannot write to a RO FS.
    wrongPathFails('/');
  }

  private void wrongPathFails(char invalidCharacter) {
    String invalidPath = invalidCharacter + "wrong-path";
    assertThatThrownBy(() -> compile(invalidPath))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Error while creating directory with path '%s'", invalidPath + File.separator + "docs");
  }

  @Test
  void noClass(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "package pkg;");

    assertThat(
            outputPath
                .resolve("docs" + File.separator + "bugpattern-CompilerBasedTestInput.json")
                .toFile()
                .exists())
        .isFalse();
  }

  @Test
  void noJsonExpected(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(
        outputPath.toString(),
        "package pkg;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(
            outputPath
                .resolve("docs " + File.separator + "bugpattern-CompilerBasedTestInput.json")
                .toFile()
                .exists())
        .isFalse();
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

    CharSequence expectedJson =
        FileObjects.forResource(getClass(), "bugpattern_example_minimal_testdata.json")
            .getCharContent(true);

    assertThat(
            Files.readString(
                outputPath.resolve(
                    "docs" + File.separator + "bugpattern-CompilerBasedTestInput.json")))
        .isEqualToIgnoringWhitespace(expectedJson);
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

    CharSequence expectedJson =
        FileObjects.forResource(getClass(), "bugpattern_example_testdata.json")
            .getCharContent(true);

    assertThat(
            Files.readString(
                outputPath.resolve(
                    "docs" + File.separator + "bugpattern-CompilerBasedTestInput.json")))
        .isEqualToIgnoringWhitespace(expectedJson);
  }
}
