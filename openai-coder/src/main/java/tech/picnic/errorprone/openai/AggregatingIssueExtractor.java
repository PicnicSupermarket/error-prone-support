package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;

// XXX: Use this class, or drop it.
final class AggregatingIssueExtractor implements IssueExtractor {
  private final ImmutableSet<IssueExtractor> delegates;

  AggregatingIssueExtractor(ImmutableSet<IssueExtractor> delegates) {
    this.delegates = delegates;
  }

  @Override
  public Stream<Issue> extract(String str) {
    return delegates.stream().flatMap(delegate -> delegate.extract(str));
  }
}
