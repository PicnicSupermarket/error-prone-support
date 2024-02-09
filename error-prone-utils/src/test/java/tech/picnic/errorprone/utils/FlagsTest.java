package tech.picnic.errorprone.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.ErrorProneOptions;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class FlagsTest {
  private static Stream<Arguments> getListTestCases() {
    /* { args, flag, expected } */
    return Stream.of(
        arguments(ImmutableList.of(), "Foo", ImmutableList.of()),
        arguments(ImmutableList.of("-XepOpt:Foo=bar,baz"), "Qux", ImmutableList.of()),
        arguments(ImmutableList.of("-XepOpt:Foo="), "Foo", ImmutableList.of()),
        arguments(ImmutableList.of("-XepOpt:Foo=bar"), "Foo", ImmutableList.of("bar")),
        arguments(ImmutableList.of("-XepOpt:Foo=bar,baz"), "Foo", ImmutableList.of("bar", "baz")),
        arguments(ImmutableList.of("-XepOpt:Foo=,"), "Foo", ImmutableList.of("", "")));
  }

  @MethodSource("getListTestCases")
  @ParameterizedTest
  void getList(ImmutableList<String> args, String flag, ImmutableList<String> expected) {
    assertThat(Flags.getList(ErrorProneOptions.processArgs(args).getFlags(), flag))
        .containsExactlyElementsOf(expected);
  }
}
