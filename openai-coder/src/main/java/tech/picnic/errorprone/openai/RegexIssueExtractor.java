package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Document contract
final class RegexIssueExtractor implements IssueExtractor<String> {
  private final Pattern pattern;

  RegexIssueExtractor(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Stream<Issue<String>> extract(String str) {
    return Stream.of(pattern.matcher(str))
        .filter(Matcher::matches)
        .map(
            matcher ->
                new Issue<>(
                    matcher.group("message").strip(),
                    matcher.group("file"),
                    parseIntGroup(matcher, "line"),
                    parseIntGroup(matcher, "column")));
  }

  private static OptionalInt parseIntGroup(Matcher matcher, String group) {
    return matcher.group(group) == null
        ? OptionalInt.empty()
        : OptionalInt.of(Integer.parseInt(matcher.group(group)));
  }
}
