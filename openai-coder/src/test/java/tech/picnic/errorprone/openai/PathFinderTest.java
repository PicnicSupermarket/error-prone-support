package tech.picnic.errorprone.openai;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class PathFinderTest {
  private static final String WORK_DIRECTORY = "/project-root";

  private final FileSystem fileSystem =
      Jimfs.newFileSystem(
          Configuration.unix().toBuilder().setWorkingDirectory(WORK_DIRECTORY).build());
  private final PathFinder pathFinder = new PathFinder(fileSystem, fileSystem.getPath(""));

  private static Stream<Arguments> findPathTestCases() {
    /* { existingPaths, pathSuffix, expected } */
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

    assertThat(pathFinder.findPath(pathSuffix)).map(Path::toString).isEqualTo(expected);
  }

  private static void createEmptyFile(Path path) throws IOException {
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.writeString(path, "", UTF_8);
  }
}
