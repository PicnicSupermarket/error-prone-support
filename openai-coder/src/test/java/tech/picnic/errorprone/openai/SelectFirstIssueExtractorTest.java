package tech.picnic.errorprone.openai;

import static com.google.common.collect.ImmutableSet.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

final class SelectFirstIssueExtractorTest {
  private static Stream<Arguments> extractTestCases() {
    /* { delegates, expected } */
    return Stream.of(
        arguments(of(), "", Optional.empty()),
        arguments(of(charIssueExtractor(0)), "foo", Optional.of(issue("f"))),
        arguments(of(charIssueExtractor(3)), "foo", Optional.empty()),
        arguments(of(charIssueExtractor(0), charIssueExtractor(1)), "foo", Optional.of(issue("f"))),
        arguments(of(charIssueExtractor(1), charIssueExtractor(0)), "foo", Optional.of(issue("o"))),
        arguments(of(charIssueExtractor(3), charIssueExtractor(0)), "foo", Optional.of(issue("f"))),
        arguments(of(charIssueExtractor(3), charIssueExtractor(4)), "foo", Optional.empty()),
        arguments(
            of(charIssueExtractor(0), errorIssueExtractor()), "foo", Optional.of(issue("f"))));
  }

  @MethodSource("extractTestCases")
  @ParameterizedTest
  void extract(
      ImmutableSet<IssueExtractor<String>> delegates,
      String input,
      Optional<Issue<String>> expected) {
    assertThat(new SelectFirstIssueExtractor<>(delegates).extract(input))
        .containsExactlyElementsOf(expected.stream().collect(toImmutableSet()));
  }

  private static IssueExtractor<String> charIssueExtractor(int retainedChar) {
    return str ->
        Stream.of(str)
            .filter(s -> s.length() > retainedChar)
            .map(s -> issue(String.valueOf(s.charAt(retainedChar))));
  }

  private static IssueExtractor<String> errorIssueExtractor() {
    return str -> {
      throw new AssertionError("This should not be called");
    };
  }

  private static Issue<String> issue(String message) {
    return new Issue<>("", OptionalInt.empty(), OptionalInt.empty(), message);
  }
}
