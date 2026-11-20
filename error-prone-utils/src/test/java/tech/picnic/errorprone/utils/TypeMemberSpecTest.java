package tech.picnic.errorprone.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TypeMemberSpecTest {
  private static Stream<Arguments> parseTestCases() {
    /* { spec, expected } */
    return Stream.of(
        arguments(
            Math.class.getCanonicalName(),
            new TypeMemberSpec(Math.class.getCanonicalName(), Optional.empty())),
        arguments(
            "java.lang.Math#max",
            new TypeMemberSpec(Math.class.getCanonicalName(), Optional.of("max"))),
        arguments("#max", new TypeMemberSpec("", Optional.of("max"))),
        arguments(
            "java.lang.Math#max#min",
            new TypeMemberSpec(Math.class.getCanonicalName(), Optional.of("max#min"))));
  }

  @MethodSource("parseTestCases")
  @ParameterizedTest
  void parse(String spec, TypeMemberSpec expected) {
    assertThat(TypeMemberSpec.parse(spec)).isEqualTo(expected);
  }
}
