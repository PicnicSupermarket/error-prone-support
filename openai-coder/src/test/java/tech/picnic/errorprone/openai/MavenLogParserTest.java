package tech.picnic.errorprone.openai;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class MavenLogParserTest {
  private static final String WORK_DIRECTORY = "/project-root";

  // XXX: Review whether we want these as fields or local variables.
  private final FileSystem fileSystem =
      Jimfs.newFileSystem(
          Configuration.unix().toBuilder().setWorkingDirectory(WORK_DIRECTORY).build());
  private final MavenLogParser parser = new MavenLogParser(fileSystem, fileSystem.getPath(""));

  private static Stream<Arguments> extractIssuesTestCases() {
    return Stream.of(
        arguments("""
            Line without log level
            """, ImmutableSet.of()),
        arguments("""
            [INFO] Line with log level
            """, ImmutableSet.of()),
        arguments(
            """
            [WARNING] Line with warning
            """,
            ImmutableSet.of("[WARNING] Line with warning")),
        arguments(
            """
            [ERROR] Line with error
            """,
            ImmutableSet.of("[ERROR] Line with error")),
        arguments(
            """
            [INFO] Info 1
            [INFO] Info 2
            [WARNING] Warning 1
            [ERROR] Error 1
            [INFO] Info 3
            """,
            ImmutableSet.of("[WARNING] Warning 1", "[ERROR] Error 1")),
        arguments(
            """
            [INFO] Info 1
            Info line 1, continued
            [INFO] Info 2
            [WARNING] Warning 1
            Warning line 1, continued
            [ERROR] Error 1
            Error line 1, continued
            Error line 1, continued still
            [INFO] Info 3
            [WARNING] Warning 2
            """,
            ImmutableSet.of(
                """
                [WARNING] Warning 1
                Warning line 1, continued""",
                """
                [ERROR] Error 1
                Error line 1, continued
                Error line 1, continued still""",
                """
                [WARNING] Warning 2""")));
  }

  @ParameterizedTest
  @MethodSource("extractIssuesTestCases")
  void extractIssues(String logs, ImmutableSet<String> expectedIssues) throws IOException {
    assertThat(MavenLogParser.extractIssues(new ByteArrayInputStream(logs.getBytes(UTF_8))))
        .containsExactlyElementsOf(expectedIssues);
  }

  private static Stream<Arguments> findPathTestCases() {
    return Stream.of(
        arguments(ImmutableSet.of(), "foo.txt", Optional.empty()),
        arguments(ImmutableSet.of("/foo.txt"), "foo.txt", Optional.empty()),
        arguments(ImmutableSet.of("/foo.txt"), "/foo.txt", Optional.of("/foo.txt")),
        arguments(ImmutableSet.of("foo.txt"), "foo.txt", Optional.of(WORK_DIRECTORY + "/foo.txt")),
        arguments(ImmutableSet.of("bar.txt"), "foo.txt", Optional.empty()),
        arguments(
            ImmutableSet.of("foo.txt", "bar.txt"),
            "foo.txt",
            Optional.of(WORK_DIRECTORY + "/foo.txt")),
        arguments(
            ImmutableSet.of("x/y/foo.txt"),
            "foo.txt",
            Optional.of(WORK_DIRECTORY + "/x/y/foo.txt")),
        arguments(ImmutableSet.of("x/foo.txt", "x/y/foo.txt"), "foo.txt", Optional.empty()),
        arguments(
            ImmutableSet.of("x/foo.txt", "x/y/foo.txt"),
            "x/foo.txt",
            Optional.of(WORK_DIRECTORY + "/x/foo.txt")));
  }

  @MethodSource("findPathTestCases")
  @ParameterizedTest
  void findPath(ImmutableSet<String> existingPaths, String pathSuffix, Optional<String> expected)
      throws IOException {
    for (String existingPath : existingPaths) {
      createEmptyFile(fileSystem.getPath(existingPath));
    }

    assertThat(parser.findPath(pathSuffix)).map(Path::toString).isEqualTo(expected);
  }

  private static void createEmptyFile(Path path) throws IOException {
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.writeString(path, "", UTF_8);
  }
}
