package tech.picnic.errorprone.bugpatterns;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
        Arrays.class, Joiner.class, StandardCharsets.class, Stream.class, Streams.class, joining());
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
        Joiner.on("b").join(new CharSequence[] {"baz", "qux"}),
        Stream.of(new String[] {"foo", "bar"}).collect(joining("c")),
        Arrays.stream(new CharSequence[] {"baz", "qux"}).collect(joining("d")),
        Joiner.on("e").join(ImmutableList.of("foo", "bar")),
        Streams.stream(Iterables.cycle(ImmutableList.of("foo", "bar"))).collect(joining("f")),
        ImmutableList.of("baz", "qux").stream().collect(joining("g")));
  }

  String testSubstringRemainder() {
    return "foo".substring(1, "foo".length());
  }

  int testUtf8EncodedLength() {
    return "foo".getBytes(StandardCharsets.UTF_8).length;
  }
}
