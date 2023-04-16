package tech.picnic.errorprone.openai;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MavenLogParser {

  // XXX: Use.
  MavenLogParser() {}

  ////////////////////////////////////////////////////

  // XXX: Review class name.
  // XXX: Provide additional implementations.
  static final class JavacAndCheckstyleLogLineAnalyzer implements LogLineAnalyzer {
    private static final Pattern FILE_LOCATION_MARKER =
        Pattern.compile("^(.*?\\.java):\\[(\\d+)(?:,(\\d+))?\\] ");

    @Override
    public Optional<Issue> analyze(String logLine) {
      Matcher matcher = FILE_LOCATION_MARKER.matcher(logLine);
      if (!matcher.find()) {
        return Optional.empty();
      }

      // XXX: Better use the `PathFinder`...
      Optional<Path> path =
          new PathFinder(FileSystems.getDefault(), Path.of("")).findPath(matcher.group(1));

      return path.map(
          p ->
              new Issue(
                  logLine.substring(matcher.end()),
                  p,
                  Integer.parseInt(matcher.group(2)),
                  matcher.group(3) == null
                      ? OptionalInt.empty()
                      : OptionalInt.of(Integer.parseInt(matcher.group(3)))));
    }
  }

  interface LogLineAnalyzer {
    Optional<Issue> analyze(String line);
  }

  record Issue(String message, Path file, int line, OptionalInt column) {}
}
