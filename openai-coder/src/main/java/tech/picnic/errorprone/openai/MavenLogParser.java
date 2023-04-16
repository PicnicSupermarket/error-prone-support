package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class MavenLogParser {
  private final ImmutableSet<LogLineAnalyzer> logLineAnalyzers;

  // XXX: Use.
  MavenLogParser(ImmutableSet<LogLineAnalyzer> logLineAnalyzers) {
    this.logLineAnalyzers = logLineAnalyzers;
  }

  ////////////////////////////////////////////////////

  // XXX: Create a second `LogLineAnalyzer` for Error Prone test compiler output.

  // XXX: Review class name.
  // XXX: Provide additional implementations.
  static final class JavacAndCheckstyleLogLineAnalyzer implements LogLineAnalyzer {
    private static final Pattern FILE_LOCATION_MARKER =
        Pattern.compile("^(.*?\\.java):\\[(\\d+)(?:,(\\d+))?\\] ");

    private final PathFinder pathFinder;

    JavacAndCheckstyleLogLineAnalyzer(PathFinder pathFinder) {
      this.pathFinder = pathFinder;
    }

    @Override
    public Stream<Issue> analyze(String logLine) {
      Matcher matcher = FILE_LOCATION_MARKER.matcher(logLine);
      if (!matcher.find()) {
        return Stream.empty();
      }

      return pathFinder
          .findPath(matcher.group(1))
          .map(
              p ->
                  new Issue(
                      logLine.substring(matcher.end()),
                      p,
                      Integer.parseInt(matcher.group(2)),
                      matcher.group(3) == null
                          ? OptionalInt.empty()
                          : OptionalInt.of(Integer.parseInt(matcher.group(3)))))
          .stream();
    }
  }

  // XXX: Move to separate file.
  interface LogLineAnalyzer {
    Stream<Issue> analyze(String line);
  }

  // XXX: Make `Path` a `String` and do path lookup post collection? (This would simplify things,
  // but may close off some future possibilities.)
  // ^ Not really. Where it matters we can double-resolve.
  // XXX: Move to separate file, or inside `LogLineAnalyzer`.
  record Issue(String message, Path file, int line, OptionalInt column) {}
}
