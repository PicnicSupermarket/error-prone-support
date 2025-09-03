package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.attribute.AclEntryPermission.ADD_SUBDIRECTORY;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases;

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
                            Sets.difference(
                                entry.permissions(), Sets.immutableEnumSet(ADD_SUBDIRECTORY)))
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
    String actualOutputDirectory = outputDirectory.toAbsolutePath() + " extra-arg";
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    actualOutputDirectory, "A.java", "package pkg;"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Precisely one path must be provided");
  }

  @Test
  void skipOnError(@TempDir Path outputDirectory) {
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    outputDirectory,
                    "A.java",
                    "class A {",
                    "  void m() {",
                    "    nonExistentMethod();",
                    "  }",
                    "}"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContainingAll("error: cannot find symbol", "nonExistentMethod()");

    assertThat(outputDirectory).isEmptyDirectory();
  }

  @Test
  void skipPackageInfo(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory, "package-info.java", "package pkg;");

    assertThat(outputDirectory).isEmptyDirectory();
  }

  @Test
  void extraction(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "DocumentationGeneratorTaskListenerTestClass.java",
        "class DocumentationGeneratorTaskListenerTestClass {}");

    assertThat(
            outputDirectory.resolve(
                "documentation-generator-task-listener-test-DocumentationGeneratorTaskListenerTestClass.json"))
        .content(UTF_8)
        .isEqualToIgnoringWhitespace(
            """
            {
              "source": "class://DocumentationGeneratorTaskListenerTestClass",
              "testClass": "CLASS: DocumentationGeneratorTaskListenerTestClass, COMPILATION_UNIT",
              "testCases": []
            }
            """);
  }

  @AutoService(Extractor.class)
  @SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
  public record TestExtractor() implements Extractor<ProjectInfo> {
    @Override
    public String identifier() {
      return "documentation-generator-task-listener-test";
    }

    @Override
    public Optional<ProjectInfo> tryExtract(ClassTree tree, VisitorState state) {
      return Optional.of(tree.getSimpleName().toString())
          .filter(n -> n.contains(DocumentationGeneratorTaskListenerTest.class.getSimpleName()))
          .map(
              className ->
                  new BugPatternTestCases(
                      URI.create("class://" + className),
                      Streams.stream(state.getPath())
                          .map(TestExtractor::describeTree)
                          .collect(joining(", ")),
                      ImmutableList.of()));
    }

    private static String describeTree(Tree tree) {
      return (tree instanceof ClassTree clazz)
          ? String.join(": ", String.valueOf(tree.getKind()), clazz.getSimpleName())
          : tree.getKind().toString();
    }
  }
}
