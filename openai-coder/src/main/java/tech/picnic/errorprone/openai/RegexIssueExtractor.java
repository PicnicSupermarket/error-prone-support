package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// XXX: Document contract
final class RegexIssueExtractor implements IssueExtractor {
  private final Pattern pattern;

  RegexIssueExtractor(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Stream<Issue> extract(String str) {
    return Stream.of(pattern.matcher(str))
        .filter(Matcher::matches)
        .map(
            matcher ->
                new IssueExtractor.Issue(
                    matcher.group("message"),
                    matcher.group("file"),
                    Integer.parseInt(matcher.group("line")),
                    matcher.group("column") == null
                        ? OptionalInt.empty()
                        : OptionalInt.of(Integer.parseInt(matcher.group("column")))));
  }
}
