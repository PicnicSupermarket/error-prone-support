package tech.picnic.errorprone.refastertemplates;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link String}s. */
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
    String before(String delimiter, Collection<? extends CharSequence> elements) {
      return elements.stream().collect(joining(delimiter));
    }

    @AfterTemplate
    String after(String delimiter, Iterable<? extends CharSequence> elements) {
      return String.join(delimiter, elements);
    }
  }
}
