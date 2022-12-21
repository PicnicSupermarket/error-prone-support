package tech.picnic.errorprone.bugpatterns.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class JavaKeywordsTest {
  private static Stream<Arguments> isValidIdentifierTestCases() {
    /* { str, expected } */
    return Stream.of(
        arguments("", false),
        arguments("public", false),
        arguments("true", false),
        arguments("false", false),
        arguments("null", false),
        arguments("0", false),
        arguments("\0", false),
        arguments("a%\0", false),
        arguments("a", true),
        arguments("a0", true),
        arguments("_a0", true),
        arguments("test", true));
  }

  @MethodSource("isValidIdentifierTestCases")
  @ParameterizedTest
  void isValidIdentifier(String str, boolean expected) {
    assertThat(JavaKeywords.isValidIdentifier(str)).isEqualTo(expected);
  }
}
