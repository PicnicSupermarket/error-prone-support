package tech.picnic.errorprone.refastertemplates;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Utf8;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link String}s. */
// XXX: Should we prefer `s -> !s.isEmpty()` or `not(String::isEmpty)`?
final class StringTemplates {
  private StringTemplates() {}

  /** Prefer {@link String#isEmpty()} over alternatives that consult the string's length. */
  static final class StringIsEmpty {
    @BeforeTemplate
    boolean before(String str) {
      return Refaster.anyOf(str.length() == 0, str.length() <= 0, str.length() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String str) {
      return str.isEmpty();
    }
  }

  /** Prefer {@link Strings#isNullOrEmpty(String)} over the more verbose alternative. */
  static final class StringIsNullOrEmpty {
    @BeforeTemplate
    boolean before(String str) {
      return str == null || str.isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String str) {
      return Strings.isNullOrEmpty(str);
    }
  }

  /** Don't use the ternary operator to create an optionally-absent string. */
  // XXX: This is a special case of `TernaryOperatorOptionalNegativeFiltering`.
  static final class OptionalNonEmptyString {
    @BeforeTemplate
    Optional<String> before(String str) {
      return Strings.isNullOrEmpty(str)
          ? Optional.empty()
          : Refaster.anyOf(Optional.of(str), Optional.ofNullable(str));
    }

    @AfterTemplate
    Optional<String> after(String str) {
      return Optional.ofNullable(str).filter(s -> !s.isEmpty());
    }
  }

  static final class FilterEmptyString {
    @BeforeTemplate
    Optional<String> before(Optional<String> optional) {
      return optional.map(Strings::emptyToNull);
    }

    @AfterTemplate
    Optional<String> after(Optional<String> optional) {
      return optional.filter(s -> !s.isEmpty());
    }
  }

  /** Prefer {@link String#join(CharSequence, Iterable)} and variants over the Guava alternative. */
  // XXX: Joiner.on(char) isn't rewritten. Add separate rule?
  // XXX: Joiner#join(@Nullable Object first, @Nullable Object second, Object... rest) isn't
  // rewritten.
  static final class JoinStrings {
    // XXX: Drop the inner `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    String before(String delimiter, CharSequence[] elements) {
      return Refaster.anyOf(
          Joiner.on(delimiter).join(elements),
          Refaster.anyOf(Stream.of(elements), Arrays.stream(elements)).collect(joining(delimiter)));
    }

    @BeforeTemplate
    String before(String delimiter, Iterable<? extends CharSequence> elements) {
      return Refaster.anyOf(
          Joiner.on(delimiter).join(elements),
          Streams.stream(elements).collect(joining(delimiter)));
    }

    @BeforeTemplate
    String before(CharSequence delimiter, Collection<? extends CharSequence> elements) {
      return elements.stream().collect(joining(delimiter));
    }

    @AfterTemplate
    String after(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
      return String.join(delimiter, elements);
    }
  }

  /** Don't unnecessarily use the two-argument {@link String#substring(int, int)}. */
  static final class SubstringRemainder {
    @BeforeTemplate
    String before(String str, int index) {
      return str.substring(index, str.length());
    }

    @AfterTemplate
    String after(String str, int index) {
      return str.substring(index);
    }
  }

  /** Prefer {@link Utf8#encodedLength(CharSequence)} over less efficient alternatives. */
  static final class Utf8EncodedLength {
    @BeforeTemplate
    int before(String str) {
      return str.getBytes(StandardCharsets.UTF_8).length;
    }

    @AfterTemplate
    int after(String str) {
      return Utf8.encodedLength(str);
    }
  }
}
