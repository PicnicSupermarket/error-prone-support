package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;

// XXX: Use this class, or drop it.
final class AggregatingIssueExtractor<T> implements IssueExtractor<T> {
  private final ImmutableSet<IssueExtractor<T>> delegates;

  AggregatingIssueExtractor(ImmutableSet<IssueExtractor<T>> delegates) {
    this.delegates = delegates;
  }

  @Override
  public Stream<Issue<T>> extract(String str) {
    return delegates.stream().flatMap(delegate -> delegate.extract(str));
  }
}
