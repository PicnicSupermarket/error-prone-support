package tech.picnic.errorprone.bugpatterns;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class StringTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Joiner.class, Stream.class, Streams.class, joining(), UTF_8);
  }

  ImmutableSet<Boolean> testStringIsEmpty() {
    return ImmutableSet.of(
        "foo".length() == 0,
        "bar".length() <= 0,
        "baz".length() < 1,
        "foo".length() != 0,
        "bar".length() > 0,
        "baz".length() >= 1);
  }

  ImmutableSet<Boolean> testStringIsNullOrEmpty() {
    return ImmutableSet.of(
        getClass().getName() == null || getClass().getName().isEmpty(),
        getClass().getName() != null && !getClass().getName().isEmpty());
  }

  ImmutableSet<Optional<String>> testOptionalNonEmptyString() {
    return ImmutableSet.of(
        Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.of(toString()),
        Strings.isNullOrEmpty(toString()) ? Optional.empty() : Optional.ofNullable(toString()),
        !Strings.isNullOrEmpty(toString()) ? Optional.of(toString()) : Optional.empty(),
        !Strings.isNullOrEmpty(toString()) ? Optional.ofNullable(toString()) : Optional.empty());
  }

  Optional<String> testFilterEmptyString() {
    return Optional.of("foo").map(Strings::emptyToNull);
  }

  ImmutableSet<String> testJoinStrings() {
    return ImmutableSet.of(
        Joiner.on("a").join(new String[] {"foo", "bar"}),
        Joiner.on("b").join(new CharSequence[] {"foo", "bar"}),
        Arrays.stream(new String[] {"foo", "bar"}).collect(joining("c")),
        Joiner.on("d").join(ImmutableList.of("foo", "bar")),
        Streams.stream(Iterables.cycle(ImmutableList.of("foo", "bar"))).collect(joining("e")),
        ImmutableList.of("foo", "bar").stream().collect(joining("f")));
  }

  String testSubstringRemainder() {
    return "foo".substring(1, "foo".length());
  }

  int testUtf8EncodedLength() {
    return "foo".getBytes(UTF_8).length;
  }
}
