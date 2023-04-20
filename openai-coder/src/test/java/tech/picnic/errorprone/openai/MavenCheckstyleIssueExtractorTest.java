package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

final class MavenCheckstyleIssueExtractorTest {
  private final IssueExtractor<String> issueExtractor = new MavenCheckstyleIssueExtractor();

  private static Stream<Arguments> extractTestCases() {
    /* { input, expected } */
    return Stream.of(
        arguments("", ImmutableSet.of()),
        arguments("foo", ImmutableSet.of()),
        arguments("Clazz.java: error message without location specification", ImmutableSet.of()),
        arguments("Clazz.java:[1,1]: RuleName: category indicator missing", ImmutableSet.of()),
        arguments(
            """
            path/to/MyClass.java.java:[2,10] (annotation) AnnotationUseStyle: Annotation style must be 'COMPACT_NO_ARRAY'.
            """,
            ImmutableSet.of(
                new Issue<>(
                    "path/to/MyClass.java.java",
                    OptionalInt.of(2),
                    OptionalInt.of(10),
                    "AnnotationUseStyle: Annotation style must be 'COMPACT_NO_ARRAY'."))),
        arguments(
            """
            /absolute/path/to/MyTextFile.txt:[123] (regexp) RegexpMultiline: Avoid blank lines at the start of a block.
            """,
            ImmutableSet.of(
                new Issue<>(
                    "/absolute/path/to/MyTextFile.txt",
                    OptionalInt.of(123),
                    OptionalInt.empty(),
                    "RegexpMultiline: Avoid blank lines at the start of a block."))));
  }

  @ParameterizedTest
  @MethodSource("extractTestCases")
  void extract(String input, ImmutableSet<Issue<String>> expected) {
    assertThat(issueExtractor.extract(input)).containsExactlyElementsOf(expected);
  }
}
