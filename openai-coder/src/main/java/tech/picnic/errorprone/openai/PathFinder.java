package tech.picnic.errorprone.openai;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PathFinder {
  private final FileSystem fileSystem;
  private final Path projectRoot;

  PathFinder(FileSystem fileSystem, Path projectRoot) {
    this.fileSystem = fileSystem;
    this.projectRoot = projectRoot.toAbsolutePath();
  }

  Optional<Path> findPath(String pathSuffix) {
    Path path = projectRoot.resolve(pathSuffix);
    if (Files.exists(path)) {
      return Optional.of(path);
    }

    PathMatcher matcher = fileSystem.getPathMatcher("glob:**" + pathSuffix);

    List<Path> inexactMatches = new ArrayList<>();
    try {
      Files.walkFileTree(
          projectRoot,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (matcher.matches(file)) {
                inexactMatches.add(file);
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new UncheckedIOException("File walk failure", e);
    }

    // XXX: Log if not exactly one match? Could use SLF4J + SimpleLogger on the exec:java classpath.
    return Optional.of(inexactMatches)
        .filter(matches -> matches.size() == 1)
        .map(Iterables::getOnlyElement);
  }
}
