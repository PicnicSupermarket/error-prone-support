package tech.picnic.errorprone.plugin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static tech.picnic.errorprone.plugin.DocumentationGenerator.DOCS_DIRECTORY;
import static tech.picnic.errorprone.plugin.DocumentationGenerator.OUTPUT_DIRECTORY_OPTION;
import static tech.picnic.errorprone.plugin.DocumentationGenerator.getDocsPath;

import java.io.File;
import java.nio.file.InvalidPathException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class DocumentationGeneratorTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void invalidPath() {
    String basePath = '?' + "path";
    assertThatThrownBy(() -> getDocsPath(basePath))
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasRootCauseInstanceOf(InvalidPathException.class)
        .hasMessageEndingWith(
            "Error while creating directory with path '%s'",
            basePath + File.separator + DOCS_DIRECTORY);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "nothing", "invalidOption=Test", "-XdocsOutputDirectory"})
  void invalidOption(String docsPathArg) {
    assertThatThrownBy(() -> getDocsPath(docsPathArg))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("%s must be of the form '%s=<value>'", docsPathArg, OUTPUT_DIRECTORY_OPTION);
  }
}
