package tech.picnic.errorprone.documentation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static tech.picnic.errorprone.documentation.DocumentationGenerator.DOCS_DIRECTORY;
import static tech.picnic.errorprone.documentation.DocumentationGenerator.OUTPUT_DIRECTORY_FLAG;

import java.io.File;
import java.nio.file.InvalidPathException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class DocumentationGeneratorTest {
  @ParameterizedTest
  @ValueSource(strings = {"", "-XdocsOutputDirectory", "invalidOption=Test", "nothing"})
  void getDocsPath(String docsPathArg) {
    assertThatThrownBy(() -> DocumentationGenerator.getDocsPath(docsPathArg))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'%s' must be of the form '%s=<value>'", docsPathArg, OUTPUT_DIRECTORY_FLAG);
  }

  @EnabledOnOs(WINDOWS)
  @Test
  void getDocsPathOnWindow() {
    String basePath = '?' + "path";
    assertThatThrownBy(
            () -> DocumentationGenerator.getDocsPath(OUTPUT_DIRECTORY_FLAG + '=' + basePath))
        .isInstanceOf(IllegalArgumentException.class)
        .hasCauseInstanceOf(InvalidPathException.class)
        .hasMessageEndingWith("Invalid path '%s'", basePath + File.separator + DOCS_DIRECTORY);
  }
}
