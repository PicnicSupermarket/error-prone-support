package tech.picnic.errorprone.bugpatterns;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class StringTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Joiner.class,
        StandardCharsets.class,
        Stream.class,
        Streams.class,
        (Runnable) () -> joining());
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

  ImmutableSet<Boolean> testStringIsNullOrEmpty() {
    return ImmutableSet.of(
        Strings.isNullOrEmpty(getClass().getName()), !Strings.isNullOrEmpty(getClass().getName()));
  }

  ImmutableSet<Optional<String>> testOptionalNonEmptyString() {
    return ImmutableSet.of(
        Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
        Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
        Optional.ofNullable(toString()).filter(s -> !s.isEmpty()),
        Optional.ofNullable(toString()).filter(s -> !s.isEmpty()));
  }

  Optional<String> testFilterEmptyString() {
    return Optional.of("foo").filter(s -> !s.isEmpty());
  }

  ImmutableSet<String> testJoinStrings() {
    return ImmutableSet.of(
        String.join("a", new String[] {"foo", "bar"}),
        String.join("b", new CharSequence[] {"baz", "qux"}),
        String.join("c", new String[] {"foo", "bar"}),
        String.join("d", new CharSequence[] {"baz", "qux"}),
        String.join("e", ImmutableList.of("foo", "bar")),
        String.join("f", Iterables.cycle(ImmutableList.of("foo", "bar"))),
        String.join("g", ImmutableList.of("baz", "qux")));
  }

  String testSubstringRemainder() {
    return "foo".substring(1);
  }

  int testUtf8EncodedLength() {
    return Utf8.encodedLength("foo");
  }
}
