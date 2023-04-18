package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

final class RegexIssueExtractorTest {
  private static Stream<Arguments> extractTestCases() {
    /* { pattern, input, expected } */
    return Stream.of(
        arguments("(?<file>.*):(?<message>.*)", "", ImmutableSet.of()),
        arguments(
            "(?<file>.*?)(:(?<line>\\d+))?(:(?<column>\\d+))?:(?<message>.*?)",
            "my-file:my-message",
            ImmutableSet.of(
                new Issue<>("my-file", OptionalInt.empty(), OptionalInt.empty(), "my-message"))),
        arguments(
            "(?<file>.*?)(:(?<line>\\d+))?(:(?<column>\\d+))?:(?<message>.*?)",
            "foo:1:bar",
            ImmutableSet.of(new Issue<>("foo", OptionalInt.of(1), OptionalInt.empty(), "bar"))),
        arguments(
            "(?<file>.*?)(:(?<line>\\d+))?(:(?<column>\\d+))?:(?<message>.*?)",
            "x:1:2:y",
            ImmutableSet.of(new Issue<>("x", OptionalInt.of(1), OptionalInt.of(2), "y"))));
  }

  @ParameterizedTest
  @MethodSource("extractTestCases")
  void extract(String pattern, String input, ImmutableSet<Issue<String>> expected) {
    assertThat(new RegexIssueExtractor(Pattern.compile(pattern)).extract(input))
        .containsExactlyElementsOf(expected);
  }

  private static Stream<Arguments> extractWithMissingNamedGroupTestCases() {
    /* { pattern, missingGroup } */
    return Stream.of(
        arguments("(?<file>.*)(?<line>\\d+)(?<column>\\d+)", "message"),
        arguments("(?<file>.*)(?<line>\\d+)(?<message>.*)", "column"),
        arguments("(?<file>.*)(?<column>\\d+)(?<message>.*)", "line"),
        arguments("(?<line>\\d+)(?<column>\\d+)(?<message>.*)", "file"));
  }

  @ParameterizedTest
  @MethodSource("extractWithMissingNamedGroupTestCases")
  void extractWithMissingNamedGroup(String pattern, String missingGroup) {
    RegexIssueExtractor issueExtractor = new RegexIssueExtractor(Pattern.compile(pattern));
    Stream<Issue<String>> issues = issueExtractor.extract("42");
    assertThatThrownBy(issues::count)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No group with name <%s>", missingGroup);
  }
}
