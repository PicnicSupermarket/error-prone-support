package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.file.attribute.AclEntryPermission.ADD_SUBDIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

final class DocumentationGeneratorTaskListenerTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void readOnlyFileSystemWindows(@TempDir Path outputDirectory) throws IOException {
    AclFileAttributeView view =
        Files.getFileAttributeView(outputDirectory, AclFileAttributeView.class);
    view.setAcl(
        view.getAcl().stream()
            .map(
                entry ->
                    AclEntry.newBuilder(entry)
                        .setPermissions(
                            Sets.difference(entry.permissions(), ImmutableSet.of(ADD_SUBDIRECTORY)))
                        .build())
            .collect(toImmutableList()));

    readOnlyFileSystemFailsToWrite(outputDirectory.resolve("nonexistent"));
  }

  @DisabledOnOs(WINDOWS)
  @Test
  void readOnlyFileSystemNonWindows(@TempDir Path outputDirectory) {
    assertThat(outputDirectory.toFile().setWritable(false))
        .describedAs("Failed to make test directory unwritable")
        .isTrue();

    readOnlyFileSystemFailsToWrite(outputDirectory.resolve("nonexistent"));
  }

  private static void readOnlyFileSystemFailsToWrite(Path outputDirectory) {
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    outputDirectory, "A.java", "class A {}"))
        .hasRootCauseInstanceOf(FileSystemException.class)
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith("Error while creating directory with path '%s'", outputDirectory);
  }

  @Test
  void noClassNoOutput(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(outputDirectory, "A.java", "package pkg;");

    assertThat(outputDirectory).isEmptyDirectory();
  }

  @Test
  void excessArguments(@TempDir Path outputDirectory) {
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    outputDirectory.toAbsolutePath() + " extra-arg", "A.java", "package pkg;"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Precisely one path must be provided");
  }
}
