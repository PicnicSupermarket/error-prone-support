package tech.picnic.errorprone.refaster.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.picnic.errorprone.refaster.plugin.RefasterRuleCompilerTaskListener.REFASTER_FILE_EXTENSION;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileManagers;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.file.JavacFileManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

final class RefasterRuleCompilerTaskListenerTest {
  private static final JavaFileObject RULE_SOURCE =
      FileObjects.forSourceLines(
          "pkg/A.java",
          "package pkg;",
          "",
          "import com.google.errorprone.refaster.annotation.AfterTemplate;",
          "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
          "",
          "@Deprecated",
          "@SuppressWarnings(\"foo\")",
          "class A {",
          "  @SuppressWarnings(\"bar\")",
          "  static final class B {",
          "    static final class FooRule {",
          "      @BeforeTemplate",
          "      String before(String string) {",
          "        return string.toString();",
          "      }",
          "",
          "      @AfterTemplate",
          "      String after(String string) {",
          "        return string;",
          "      }",
          "    }",
          "  }",
          "}");

  @Test
  void compileRule(@TempDir Path outputDirectory) throws IOException, ClassNotFoundException {
    assertThat(compile(fileManager(outputDirectory), ImmutableList.of(RULE_SOURCE))).isTrue();

    try (ObjectInputStream in =
        new ObjectInputStream(
            Files.newInputStream(
                outputDirectory.resolve("pkg").resolve("A$B$FooRule" + REFASTER_FILE_EXTENSION)))) {
      @SuppressWarnings("BanSerializableRead" /* Deserializing the Refaster rule under test. */)
      AnnotatedCompositeCodeTransformer transformer =
          (AnnotatedCompositeCodeTransformer) in.readObject();
      assertThat(transformer.annotations()).containsKey(Deprecated.class);
      assertThat(transformer.annotations().getInstance(SuppressWarnings.class))
          .satisfies(annotation -> assertThat(annotation.value()).containsExactly("bar"));
    }
  }

  @Test
  void filePersistenceFailure() throws IOException {
    try (JavaFileManager fileManager = new FailingJavaFileManager()) {
      assertThatThrownBy(() -> compile(fileManager, ImmutableList.of(RULE_SOURCE)))
          .cause()
          .isInstanceOf(UncheckedIOException.class)
          .hasMessage("Failed to persist compiled Refaster rule 'FooRule'")
          .cause()
          .isInstanceOf(IOException.class)
          .hasMessage(FailingJavaFileManager.ERROR_MESSAGE);
    }
  }

  private static Stream<Arguments> noCompiledRuleIsWrittenTestCases() {
    /* { expectedSuccess, sources } */
    return Stream.of(
        arguments(
            false,
            ImmutableList.of(
                FileObjects.forSourceLines("Invalid.java", "class Invalid { int i = \"foo\"; }"),
                RULE_SOURCE)),
        arguments(
            true,
            ImmutableList.of(
                FileObjects.forSourceLines("Foo.java", "@interface Foo {}", "@Foo class Bar {}"))));
  }

  @MethodSource("noCompiledRuleIsWrittenTestCases")
  @ParameterizedTest
  void noCompiledRuleIsWritten(
      boolean expectedSuccess, ImmutableList<JavaFileObject> sources, @TempDir Path outputDirectory)
      throws IOException {
    assertThat(compile(fileManager(outputDirectory), sources)).isEqualTo(expectedSuccess);

    try (Stream<Path> files = Files.walk(outputDirectory)) {
      assertThat(files).noneMatch(file -> file.toString().endsWith(REFASTER_FILE_EXTENSION));
    }
  }

  private static JavacFileManager fileManager(Path outputDirectory) throws IOException {
    JavacFileManager fileManager = FileManagers.testFileManager();
    fileManager.setLocationFromPaths(
        StandardLocation.CLASS_OUTPUT, ImmutableList.of(outputDirectory));
    return fileManager;
  }

  private static boolean compile(
      JavaFileManager fileManager, ImmutableList<JavaFileObject> sources) {
    return JavacTool.create()
        .getTask(
            null,
            fileManager,
            null,
            ImmutableList.of("-proc:none", "-Xplugin:RefasterRuleCompiler"),
            ImmutableList.of(),
            sources)
        .call();
  }

  private static final class FailingJavaFileManager
      extends ForwardingJavaFileManager<JavacFileManager> {
    private static final String ERROR_MESSAGE = "Custom error message";

    private FailingJavaFileManager() {
      super(FileManagers.testFileManager());
    }

    @Override
    public FileObject getFileForOutput(
        Location location, String packageName, String relativeName, FileObject sibling)
        throws IOException {
      throw new IOException(ERROR_MESSAGE);
    }
  }
}
