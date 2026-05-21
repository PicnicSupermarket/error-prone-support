package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StringRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Joiner.class, Objects.class, Stream.class, Streams.class, joining(), UTF_8);
  }

  ImmutableSet<String> testEmptyString() {
    return ImmutableSet.of(
        new String(),
        new String(new byte[0], UTF_8),
        new String(new byte[] {}, UTF_8),
        new String(new char[0]),
        new String(new char[] {}));
  }

  String testStringIdentity() {
    return new String("foo");
  }

  ImmutableSet<Boolean> testStringIsEmptyWithString() {
    return ImmutableSet.of(
        "foo".length() == 0,
        "bar".length() <= 0,
        "baz".length() < 1,
        "qux".length() != 0,
        "quux".length() > 0,
        "corge".length() >= 1);
  }

  boolean testStringIsEmpty() {
    return Stream.of("foo").anyMatch(s -> s.isEmpty());
  }

  boolean testNotStringIsEmpty() {
    return Stream.of("foo").anyMatch(s -> !s.isEmpty());
  }

  ImmutableSet<Boolean> testStringsIsNullOrEmpty() {
    return ImmutableSet.of(
        getClass().getName() == null || getClass().getName().isEmpty(),
        getClass().getName() != null && !getClass().getName().isEmpty());
  }

  ImmutableSet<Boolean> testStringIsBlank() {
    return ImmutableSet.of("foo".trim().isEmpty(), !"foo".trim().isEmpty());
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableFilterNotStringIsEmpty() {
    return ImmutableSet.of(
        Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.of(toString()),
        Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.ofNullable(toString()),
        !Strings.isNullOrEmpty(toString()) ? Optional.of(toString()) : Optional.empty(),
        !Strings.isNullOrEmpty(toString()) ? Optional.ofNullable(toString()) : Optional.empty());
  }

  Optional<String> testOptionalFilterNotStringIsEmpty() {
    return Optional.of("foo").map(Strings::emptyToNull);
  }

  ImmutableSet<String> testStringJoin() {
    return ImmutableSet.of(
        Joiner.on("a").join(new String[] {"foo", "bar"}),
        Joiner.on("b").join(new CharSequence[] {"foo", "bar"}),
        Arrays.stream(new String[] {"foo", "bar"}).collect(joining("c")),
        Joiner.on("d").join(ImmutableList.of("foo", "bar")),
        Streams.stream(Iterables.cycle(ImmutableList.of("foo", "bar"))).collect(joining("e")),
        ImmutableList.of("foo", "bar").stream().collect(joining("f")));
  }

  String testStringJoinVarargs() {
    return Stream.of("foo", "bar").collect(joining(","));
  }

  String testStringValueOfWithObject() {
    return Objects.toString("foo");
  }

  ImmutableSet<String> testNewString3() {
    return ImmutableSet.of(
        String.valueOf(new char[] {'f', 'o', 'o'}, 0, 1),
        String.copyValueOf(new char[] {'b', 'a', 'r'}, 2, 3));
  }

  ImmutableSet<String> testNewString1() {
    return ImmutableSet.of(
        String.valueOf(new char[] {'f', 'o', 'o'}),
        new String(new char[] {'b', 'a', 'r'}, 0, new char[] {'b', 'a', 'r'}.length));
  }

  Function<Object, String> testStringValueOf() {
    return Objects::toString;
  }

  String testStringSubstring() {
    return "foo".substring(1, "foo".length());
  }

  int testUtf8EncodedLength() {
    return "foo".getBytes(UTF_8).length;
  }

  int testMathMaxNegativeOneStringIndexOfMinusInt() {
    return "foo".substring(1).indexOf('a');
  }

  int testMathMaxNegativeOneStringIndexOfMinusIntWithInt() {
    return "foo".substring(1, 2).indexOf('a');
  }

  int testMathMaxNegativeOneStringIndexOfMinusString() {
    return "foo".substring(1).indexOf("bar");
  }

  int testMathMaxNegativeOneStringIndexOfMinusStringWithInt() {
    return "foo".substring(1, 2).indexOf("bar");
  }

  int testMathMaxNegativeOneStringLastIndexOfMinusInt() {
    return "foo".substring(1).lastIndexOf('a');
  }

  int testMathMaxNegativeOneStringLastIndexOfMinusString() {
    return "foo".substring(1).lastIndexOf("bar");
  }

  int testStringLastIndexOfMinusOneInt() {
    return "foo".substring(0, 2).lastIndexOf('a');
  }

  int testStringLastIndexOfMinusOneString() {
    return "foo".substring(0, 2).lastIndexOf("bar");
  }

  boolean testStringStartsWith() {
    return "foo".substring(1).startsWith("bar");
  }

  ImmutableSet<String> testFormatted() {
    return ImmutableSet.of(
        String.format("Constant"),
        String.format("Number: %d", 42),
        String.format("%s" + "%s", "foo", "bar"));
  }
}
