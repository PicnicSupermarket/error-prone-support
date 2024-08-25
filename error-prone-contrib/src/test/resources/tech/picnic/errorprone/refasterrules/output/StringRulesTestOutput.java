package tech.picnic.errorprone.refasterrules.output;

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

  ImmutableSet<Boolean> testStringIsEmpty() {
    return ImmutableSet.of(
        "foo".isEmpty(),
        "bar".isEmpty(),
        "baz".isEmpty(),
        !"foo".isEmpty(),
        !"bar".isEmpty(),
        !"baz".isEmpty());
  }

  boolean testStringIsEmptyPredicate() {
    return Stream.of("foo").anyMatch(String::isEmpty);
  }

  boolean testStringIsNotEmptyPredicate() {
    return Stream.of("foo").anyMatch(not(String::isEmpty));
  }

  ImmutableSet<Boolean> testStringIsNullOrEmpty() {
    return ImmutableSet.of(
        Strings.isNullOrEmpty(getClass().getName()), !Strings.isNullOrEmpty(getClass().getName()));
  }

  ImmutableSet<Optional<String>> testOptionalNonEmptyString() {
    return ImmutableSet.of(
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)),
        Optional.ofNullable(toString()).filter(Predicate.not(String::isEmpty)));
  }

  Optional<String> testFilterEmptyString() {
    return Optional.of("foo").filter(not(String::isEmpty));
  }

  ImmutableSet<String> testJoinStrings() {
    return ImmutableSet.of(
        String.join("a", new String[] {"foo", "bar"}),
        String.join("b", new CharSequence[] {"foo", "bar"}),
        String.join("c", new String[] {"foo", "bar"}),
        String.join("d", ImmutableList.of("foo", "bar")),
        String.join("e", Iterables.cycle(ImmutableList.of("foo", "bar"))),
        String.join("f", ImmutableList.of("foo", "bar")));
  }

  String testStringValueOf() {
    return String.valueOf("foo");
  }

  ImmutableSet<String> testNewStringFromCharArraySubSequence() {
    return ImmutableSet.of(
        new String(new char[] {'f', 'o', 'o'}, 0, 1), new String(new char[] {'b', 'a', 'r'}, 2, 3));
  }

  ImmutableSet<String> testNewStringFromCharArray() {
    return ImmutableSet.of(
        new String(new char[] {'f', 'o', 'o'}), new String(new char[] {'b', 'a', 'r'}));
  }

  Function<Object, String> testStringValueOfMethodReference() {
    return String::valueOf;
  }

  String testSubstringRemainder() {
    return "foo".substring(1);
  }

  int testUtf8EncodedLength() {
    return Utf8.encodedLength("foo");
  }
}
