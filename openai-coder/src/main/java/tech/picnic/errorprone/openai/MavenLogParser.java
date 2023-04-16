package tech.picnic.errorprone.openai;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.WillClose;

final class MavenLogParser {
  private static final Pattern LOG_LINE_START_MARKER = Pattern.compile("^\\[([A-Z]+)\\] ");
  private static final ImmutableSet<String> ISSUE_LOG_LEVELS = ImmutableSet.of("ERROR", "WARNING");

  private final FileSystem fileSystem;
  private final Path projectRoot;

  MavenLogParser(FileSystem fileSystem, Path projectRoot) {
    this.fileSystem = fileSystem;
    this.projectRoot = projectRoot.toAbsolutePath();
  }

  static List<String> extractIssues(@WillClose InputStream logs) throws IOException {
    List<String> messages = new ArrayList<>();

    boolean shouldRead = false;
    StringBuilder nextMessage = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(logs, UTF_8))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        Optional<String> logLevel = getLogLevel(line);

        if (logLevel.isPresent()) {
          if (!nextMessage.isEmpty()) {
            messages.add(nextMessage.toString());
            nextMessage.setLength(0);
          }

          shouldRead = ISSUE_LOG_LEVELS.contains(logLevel.orElseThrow());
        }

        if (shouldRead) {
          if (!nextMessage.isEmpty()) {
            nextMessage.append(System.lineSeparator());
          }
          nextMessage.append(line);
        }
      }
    }

    if (shouldRead && !nextMessage.isEmpty()) {
      messages.add(nextMessage.toString());
    }

    return messages;
  }

  private static Optional<String> getLogLevel(String logLine) {
    return Optional.of(LOG_LINE_START_MARKER.matcher(logLine))
        .filter(Matcher::find)
        .map(m -> m.group(1));
  }

  // XXX: Validate `@VisibleForTesting`.
  @VisibleForTesting
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
