package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;

/**
 * An {@link IssueExtractor} that retains only the first issue reported by a set of delegate {@link
 * IssueExtractor}s.
 */
final class SelectFirstIssueExtractor<T> implements IssueExtractor<T> {
  private final ImmutableSet<IssueExtractor<T>> delegates;

  SelectFirstIssueExtractor(ImmutableSet<IssueExtractor<T>> delegates) {
    this.delegates = delegates;
  }

  @Override
  public Stream<Issue<T>> extract(String str) {
    return delegates.stream().flatMap(delegate -> delegate.extract(str)).limit(1);
  }
}
