package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StringRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Joiner.class, Objects.class, Stream.class, Streams.class, joining(), UTF_8);
  }

  ImmutableSet<String> testEmptyString() {
    return ImmutableSet.of("", "", "", "", "");
  }

  String testStringIdentity() {
    return "foo";
  }

  ImmutableSet<Boolean> testStringIsEmptyWithString() {
    return ImmutableSet.of(
        "foo".isEmpty(),
        "bar".isEmpty(),
        "baz".isEmpty(),
        !"qux".isEmpty(),
        !"quux".isEmpty(),
        !"corge".isEmpty());
  }

  boolean testStringIsEmpty() {
    return Stream.of("foo").anyMatch(String::isEmpty);
  }

  boolean testNotStringIsEmpty() {
    return Stream.of("foo").anyMatch(not(String::isEmpty));
  }

  ImmutableSet<Boolean> testStringsIsNullOrEmpty() {
    return ImmutableSet.of(
        Strings.isNullOrEmpty(getClass().getName()), !Strings.isNullOrEmpty(getClass().getName()));
  }

  ImmutableSet<Boolean> testStringIsBlank() {
    return ImmutableSet.of("foo".isBlank(), !"foo".isBlank());
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableFilterNotStringIsEmpty() {
    return ImmutableSet.of(
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)));
  }

  Optional<String> testOptionalFilterNotStringIsEmpty() {
    return Optional.of("foo").filter(not(String::isEmpty));
  }

  ImmutableSet<String> testStringJoin() {
    return ImmutableSet.of(
        String.join("a", new String[] {"foo", "bar"}),
        String.join("b", new CharSequence[] {"foo", "bar"}),
        String.join("c", new String[] {"foo", "bar"}),
        String.join("d", ImmutableList.of("foo", "bar")),
        String.join("e", Iterables.cycle(ImmutableList.of("foo", "bar"))),
        String.join("f", ImmutableList.of("foo", "bar")));
  }

  String testStringJoinVarargs() {
    return String.join(",", "foo", "bar");
  }

  String testStringValueOfWithObject() {
    return String.valueOf("foo");
  }

  ImmutableSet<String> testNewString3() {
    return ImmutableSet.of(
        new String(new char[] {'f', 'o', 'o'}, 0, 1), new String(new char[] {'b', 'a', 'r'}, 2, 3));
  }

  ImmutableSet<String> testNewString1() {
    return ImmutableSet.of(
        new String(new char[] {'f', 'o', 'o'}), new String(new char[] {'b', 'a', 'r'}));
  }

  Function<Object, String> testStringValueOf() {
    return String::valueOf;
  }

  String testStringSubstring() {
    return "foo".substring(1);
  }

  int testUtf8EncodedLength() {
    return Utf8.encodedLength("foo");
  }

  int testMathMaxNegativeOneStringIndexOfMinusInt() {
    return Math.max(-1, "foo".indexOf('a', 1) - 1);
  }

  int testMathMaxNegativeOneStringIndexOfMinusIntWithInt() {
    return Math.max(-1, "foo".indexOf('a', 1, 2) - 1);
  }

  int testMathMaxNegativeOneStringIndexOfMinusString() {
    return Math.max(-1, "foo".indexOf("bar", 1) - 1);
  }

  int testMathMaxNegativeOneStringIndexOfMinusStringWithInt() {
    return Math.max(-1, "foo".indexOf("bar", 1, 2) - 1);
  }

  int testMathMaxNegativeOneStringLastIndexOfMinusInt() {
    return Math.max(-1, "foo".lastIndexOf('a') - 1);
  }

  int testMathMaxNegativeOneStringLastIndexOfMinusString() {
    return Math.max(-1, "foo".lastIndexOf("bar") - 1);
  }

  int testStringLastIndexOfMinusOneInt() {
    return "foo".lastIndexOf('a', 2 - 1);
  }

  int testStringLastIndexOfMinusOneString() {
    return "foo".lastIndexOf("bar", 2 - 1);
  }

  boolean testStringStartsWith() {
    return "foo".startsWith("bar", 1);
  }

  ImmutableSet<String> testFormatted() {
    return ImmutableSet.of(
        ("Constant").formatted(),
        ("Number: %d").formatted(42),
        ("%s" + "%s").formatted("foo", "bar"));
  }
}
