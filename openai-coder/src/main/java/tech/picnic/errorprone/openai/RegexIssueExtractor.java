package tech.picnic.errorprone.openai;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An {@link IssueExtractor} that delegates issue property extraction to a {@link Pattern}.
 *
 * <p>The pattern must have the following named groups:
 *
 * <ul>
 *   <li>{@code message}: The issue message.
 *   <li>{@code file}: The file to which the issue applies.
 *   <li>{@code line}: The line at which the issue occurs, if known.
 *   <li>{@code column}: The column at which the issue occurs, if known.
 * </ul>
 */
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
                    matcher.group("file"),
                    parseIntGroup(matcher, "line"),
                    parseIntGroup(matcher, "column"),
                    matcher.group("message").strip()));
  }

  private static OptionalInt parseIntGroup(Matcher matcher, String group) {
    return matcher.group(group) == null
        ? OptionalInt.empty()
        : OptionalInt.of(Integer.parseInt(matcher.group(group)));
  }
}
