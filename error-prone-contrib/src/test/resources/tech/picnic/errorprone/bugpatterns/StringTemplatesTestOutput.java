package tech.picnic.errorprone.bugpatterns;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Utf8;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class StringTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Joiner.class, Stream.class, Streams.class, joining(), UTF_8);
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
        String.join("b", new CharSequence[] {"foo", "bar"}),
        String.join("c", new String[] {"foo", "bar"}),
        String.join("d", ImmutableList.of("foo", "bar")),
        String.join("e", Iterables.cycle(ImmutableList.of("foo", "bar"))),
        String.join("f", ImmutableList.of("foo", "bar")));
  }

  String testSubstringRemainder() {
    return "foo".substring(1);
  }

  int testUtf8EncodedLength() {
    return Utf8.encodedLength("foo");
  }
}
