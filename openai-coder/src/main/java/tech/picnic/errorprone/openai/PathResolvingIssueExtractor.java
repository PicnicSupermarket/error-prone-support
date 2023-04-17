package tech.picnic.errorprone.openai;

import java.nio.file.Path;
import java.util.stream.Stream;

// XXX: Use this class, or drop it.
final class PathResolvingIssueExtractor implements IssueExtractor<Path> {
  // XXX: Do we then need `PathFinder` to be a separate class?
  private final PathFinder pathFinder;
  private final IssueExtractor<String> delegate;

  PathResolvingIssueExtractor(PathFinder pathFinder, IssueExtractor<String> delegate) {
    this.pathFinder = pathFinder;
    this.delegate = delegate;
  }

  @Override
  public Stream<Issue<Path>> extract(String str) {
    return delegate
        .extract(str)
        .flatMap(issue -> pathFinder.findPath(issue.file()).map(issue::withFile).stream());
  }
}
