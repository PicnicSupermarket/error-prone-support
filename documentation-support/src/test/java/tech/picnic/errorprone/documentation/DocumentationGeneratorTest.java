package tech.picnic.errorprone.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tech.picnic.errorprone.documentation.DocumentationGenerator.OUTPUT_DIRECTORY_FLAG;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class DocumentationGeneratorTest {
  @ParameterizedTest
  @ValueSource(strings = {"bar", "foo"})
  void getOutputPath(String path) {
    assertThat(DocumentationGenerator.getOutputPath(OUTPUT_DIRECTORY_FLAG + '=' + path))
        .isEqualTo(Path.of(path));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "-XoutputDirectory", "invalidOption=Test", "nothing"})
  void getOutputPathWithInvalidArgument(String pathArg) {
    assertThatThrownBy(() -> DocumentationGenerator.getOutputPath(pathArg))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("'%s' must be of the form '%s=<value>'", pathArg, OUTPUT_DIRECTORY_FLAG);
  }

  @Test
  void getOutputPathWithInvalidPath() {
    String basePath = "path-with-null-char-\0";
    assertThatThrownBy(
            () -> DocumentationGenerator.getOutputPath(OUTPUT_DIRECTORY_FLAG + '=' + basePath))
        .isInstanceOf(IllegalArgumentException.class)
        .hasCauseInstanceOf(InvalidPathException.class)
        .hasMessageEndingWith("Invalid path '%s'", basePath);
  }
}
