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

final class PlexusCompilerIssueExtractorTest {
  private final IssueExtractor<String> issueExtractor = new PlexusCompilerIssueExtractor();

  private static Stream<Arguments> extractTestCases() {
    /* { input, expected } */
    return Stream.of(
        arguments("", ImmutableSet.of()),
        arguments("foo", ImmutableSet.of()),
        arguments(
            """
            relative/path/to/MyClass.java:[30,22] no comment
            """,
            ImmutableSet.of()),
        arguments(
            """
            /absolute/path/to/MyClass.java:[30,22] no comment
            """,
            ImmutableSet.of(
                new Issue(
                    "/absolute/path/to/MyClass.java",
                    OptionalInt.of(30),
                    OptionalInt.of(22),
                    "no comment"))),
        arguments(
            """
            /absolute/path/to/MyClass2.java:[123] error message without column specification
            """,
            ImmutableSet.of(
                new Issue(
                    "/absolute/path/to/MyClass2.java",
                    OptionalInt.of(123),
                    OptionalInt.empty(),
                    "error message without column specification"))),
        arguments(
            """
            /absolute/path/to/MyClass3.java: error message without location specification
            """,
            ImmutableSet.of(
                new Issue(
                    "/absolute/path/to/MyClass3.java",
                    OptionalInt.empty(),
                    OptionalInt.empty(),
                    "error message without location specification"))),
        arguments(
            """
            /absolute/path/to/another/Class.java:[10,17] cannot find symbol
              symbol:   class MySymbol
              location: class another.Class
            """,
            ImmutableSet.of(
                new Issue(
                    "/absolute/path/to/another/Class.java",
                    OptionalInt.of(10),
                    OptionalInt.of(17),
                    """
                    cannot find symbol
                      symbol:   class MySymbol
                      location: class another.Class"""))),
        arguments(
            """
            /file/with/errorprone/violation/Foo.java:[2,4] [UnusedVariable] The field 'X' is never read.
               (see https://errorprone.info/bugpattern/UnusedVariable)
              Did you mean to remove this line?
            """,
            ImmutableSet.of(
                new Issue(
                    "/file/with/errorprone/violation/Foo.java",
                    OptionalInt.of(2),
                    OptionalInt.of(4),
                    """
                    [UnusedVariable] The field 'X' is never read.
                      Did you mean to remove this line?"""))));
  }

  @ParameterizedTest
  @MethodSource("extractTestCases")
  void extract(String input, ImmutableSet<Issue<String>> expected) {
    assertThat(issueExtractor.extract(input)).containsExactlyElementsOf(expected);
  }
}
