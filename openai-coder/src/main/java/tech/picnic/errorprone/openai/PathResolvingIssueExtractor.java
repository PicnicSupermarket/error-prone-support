package tech.picnic.errorprone.openai;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * An {@link IssueExtractor} that resolves delegates to another {@link IssueExtractor} and resolves
 * the extracted file paths.
 *
 * <p>For example, if the delegate extracts issues with relative file paths, this extractor will
 * resolve those paths to absolute paths.
 */
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
