package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Utf8;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link String}s. */
@OnlineDocumentation
final class StringRules {
  private StringRules() {}

  /**
   * Avoid unnecessary creation of new empty {@link String} objects; use the empty string literal
   * instead.
   */
  static final class EmptyString {
    @BeforeTemplate
    @SuppressWarnings("java:S2129" /* This violation will be rewritten. */)
    String before() {
      return Refaster.anyOf(
          new String(),
          new String(new byte[0], UTF_8),
          new String(new byte[] {}, UTF_8),
          new String(new char[0]),
          new String(new char[] {}));
    }

    @AfterTemplate
    String after() {
      return "";
    }
  }

  /** Avoid unnecessary creation of new {@link String} objects. */
  // XXX: Once `IdentityConversion` supports flagging constructor invocations, use that bug pattern
  // instead of this Refaster rule.
  static final class StringIdentity {
    @BeforeTemplate
    @SuppressWarnings("java:S2129" /* This violation will be rewritten. */)
    String before(String str) {
      return new String(str);
    }

    @AfterTemplate
    String after(String str) {
      return str;
    }
  }

  /** Prefer {@link String#isEmpty()} over alternatives that consult the string's length. */
  // XXX: Drop this rule once we (and OpenRewrite) no longer support projects targeting Java 14 or
  // below. The `CharSequenceIsEmpty` rule then suffices. (This rule exists so that e.g. projects
  // that target JDK 11 can disable `CharSequenceIsEmpty` without losing a valuable rule.)
  // XXX: Look into a more general approach to supporting different Java language levels, such as
  // rule selection based on some annotation, or a separate Maven module.
  static final class StringIsEmpty {
    @BeforeTemplate
    @SuppressWarnings({
      "CharSequenceIsEmpty" /* This is a more specific template. */,
      "java:S7158" /* This violation will be rewritten. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    boolean before(String str) {
      return Refaster.anyOf(str.length() == 0, str.length() <= 0, str.length() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String str) {
      return str.isEmpty();
    }
  }

  /** Prefer a method reference to {@link String#isEmpty()} over the equivalent lambda function. */
  // XXX: Now that we build with JDK 15+, this rule can be generalized to cover all `CharSequence`
  // subtypes. However, `CharSequence::isEmpty` isn't as nice as `String::isEmpty`, so we might want
  // to introduce a rule that suggests `String::isEmpty` where possible.
  // XXX: As it stands, this rule is a special case of what `MethodReferenceUsage` tries to achieve.
  // If/when `MethodReferenceUsage` becomes production ready, we should simply drop this check.
  static final class StringIsEmptyPredicate {
    @BeforeTemplate
    Predicate<String> before() {
      return s -> s.isEmpty();
    }

    @AfterTemplate
    Predicate<String> after() {
      return String::isEmpty;
    }
  }

  /** Prefer a method reference to {@link String#isEmpty()} over the equivalent lambda function. */
  // XXX: Now that we build with JDK 15+, this rule can be generalized to cover all `CharSequence`
  // subtypes. However, `CharSequence::isEmpty` isn't as nice as `String::isEmpty`, so we might want
  // to introduce a rule that suggests `String::isEmpty` where possible.
  static final class StringIsNotEmptyPredicate {
    @BeforeTemplate
    Predicate<String> before() {
      return s -> !s.isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Predicate<String> after() {
      return not(String::isEmpty);
    }
  }

  /** Prefer {@link Strings#isNullOrEmpty(String)} over the more verbose alternative. */
  static final class StringIsNullOrEmpty {
    @BeforeTemplate
    boolean before(@Nullable String str) {
      return str == null || str.isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String str) {
      return Strings.isNullOrEmpty(str);
    }
  }

  /** Prefer {@link String#isBlank()} over less efficient alternatives. */
  // XXX: Note that this rule changes semantics, as `isBlank()` considers whitespace characters
  // beyond U+0020, while `trim()` does not.
  static final class StringIsBlank {
    @BeforeTemplate
    boolean before(String str) {
      return str.trim().isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String str) {
      return str.isBlank();
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
      return Optional.ofNullable(str).filter(not(String::isEmpty));
    }
  }

  static final class FilterEmptyString {
    @BeforeTemplate
    Optional<String> before(Optional<String> optional) {
      return optional.map(Strings::emptyToNull);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Optional<String> after(Optional<String> optional) {
      return optional.filter(not(String::isEmpty));
    }
  }

  /** Prefer {@link String#join(CharSequence, Iterable)} and variants over the Guava alternative. */
  // XXX: Joiner.on(char) isn't rewritten. Add separate rule?
  // XXX: Joiner#join(@Nullable Object first, @Nullable Object second, Object... rest) isn't
  // rewritten.
  static final class JoinStrings {
    @BeforeTemplate
    String before(String delimiter, CharSequence[] elements) {
      return Refaster.anyOf(
          Joiner.on(delimiter).join(elements), Arrays.stream(elements).collect(joining(delimiter)));
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

  /**
   * Prefer {@link String#join(CharSequence, CharSequence...)} over {@code
   * Stream.of(...).collect(joining(...))}.
   */
  static final class StringJoinDelimiterVarargs {
    @BeforeTemplate
    String before(CharSequence delimiter, @Repeated CharSequence elements) {
      return Stream.of(Refaster.asVarargs(elements)).collect(joining(delimiter));
    }

    @AfterTemplate
    String after(CharSequence delimiter, @Repeated CharSequence elements) {
      return String.join(delimiter, Refaster.asVarargs(elements));
    }
  }

  /**
   * Prefer direct invocation of {@link String#valueOf(Object)} over the indirection introduced by
   * {@link Objects#toString(Object)}.
   */
  static final class StringValueOf {
    @BeforeTemplate
    String before(Object object) {
      return Objects.toString(object);
    }

    @AfterTemplate
    String after(Object object) {
      return String.valueOf(object);
    }
  }

  /**
   * Prefer direct invocation of {@link String#String(char[], int, int)} over the indirection
   * introduced by alternatives.
   */
  static final class NewStringFromCharArraySubSequence {
    @BeforeTemplate
    String before(char[] data, int offset, int count) {
      return Refaster.anyOf(
          String.valueOf(data, offset, count), String.copyValueOf(data, offset, count));
    }

    @AfterTemplate
    String after(char[] data, int offset, int count) {
      return new String(data, offset, count);
    }
  }

  /**
   * Prefer direct invocation of {@link String#String(char[])} over the indirection introduced by
   * alternatives.
   */
  static final class NewStringFromCharArray {
    @BeforeTemplate
    String before(char[] data) {
      return Refaster.anyOf(String.valueOf(data), new String(data, 0, data.length));
    }

    @AfterTemplate
    String after(char[] data) {
      return new String(data);
    }
  }

  /**
   * Prefer direct delegation to {@link String#valueOf(Object)} over the indirection introduced by
   * {@link Objects#toString(Object)}.
   */
  // XXX: This rule is analogous to `StringValueOf` above. Arguably this is its generalization.
  // If/when Refaster is extended to understand this, delete the rule above.
  static final class StringValueOfMethodReference {
    @BeforeTemplate
    Function<Object, String> before() {
      return Objects::toString;
    }

    @AfterTemplate
    Function<Object, String> after() {
      return String::valueOf;
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
      return str.getBytes(UTF_8).length;
    }

    @AfterTemplate
    int after(String str) {
      return Utf8.encodedLength(str);
    }
  }

  /** Prefer {@link String#indexOf(int, int)} over less efficient alternatives. */
  static final class StringIndexOfCharFromIndex {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, int ch, int fromIndex) {
      return string.substring(fromIndex).indexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int fromIndex) {
      return Math.max(-1, string.indexOf(ch, fromIndex) - fromIndex);
    }
  }

  /** Prefer {@link String#indexOf(int, int, int)} over less efficient alternatives. */
  static final class StringIndexOfCharBetweenIndices {
    @BeforeTemplate
    int before(String string, int ch, int beginIndex, int endIndex) {
      return string.substring(beginIndex, endIndex).indexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int beginIndex, int endIndex) {
      return Math.max(-1, string.indexOf(ch, beginIndex, endIndex) - beginIndex);
    }
  }

  /** Prefer {@link String#indexOf(String, int)} over less efficient alternatives. */
  static final class StringIndexOfStringFromIndex {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, String substring, int fromIndex) {
      return string.substring(fromIndex).indexOf(substring);
    }

    @AfterTemplate
    int after(String string, String substring, int fromIndex) {
      return Math.max(-1, string.indexOf(substring, fromIndex) - fromIndex);
    }
  }

  /** Prefer {@link String#indexOf(String, int)} over less efficient alternatives. */
  static final class StringIndexOfStringBetweenIndices {
    @BeforeTemplate
    int before(String string, String substring, int beginIndex, int endIndex) {
      return string.substring(beginIndex, endIndex).indexOf(substring);
    }

    @AfterTemplate
    int after(String string, String substring, int beginIndex, int endIndex) {
      return Math.max(-1, string.indexOf(substring, beginIndex, endIndex) - beginIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(int, int)} over less efficient alternatives. */
  static final class StringLastIndexOfChar {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, int ch, int fromIndex) {
      return string.substring(fromIndex).lastIndexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int fromIndex) {
      return Math.max(-1, string.lastIndexOf(ch) - fromIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(String, int)} over less efficient alternatives. */
  static final class StringLastIndexOfString {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, String substring, int fromIndex) {
      return string.substring(fromIndex).lastIndexOf(substring);
    }

    @AfterTemplate
    int after(String string, String substring, int fromIndex) {
      return Math.max(-1, string.lastIndexOf(substring) - fromIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(int, int)} over less efficient alternatives. */
  static final class StringLastIndexOfCharWithIndex {
    @BeforeTemplate
    int before(String string, int ch, int fromIndex) {
      return string.substring(0, fromIndex).lastIndexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int fromIndex) {
      return string.lastIndexOf(ch, fromIndex - 1);
    }
  }

  /** Prefer {@link String#lastIndexOf(String, int)} over less efficient alternatives. */
  // XXX: The replacement expression isn't fully equivalent: in case `substring` is empty, then
  // the replacement yields `fromIndex - 1` rather than `fromIndex`.
  static final class StringLastIndexOfStringWithIndex {
    @BeforeTemplate
    int before(String string, String substring, int fromIndex) {
      return string.substring(0, fromIndex).lastIndexOf(substring);
    }

    @AfterTemplate
    int after(String string, String substring, int fromIndex) {
      return string.lastIndexOf(substring, fromIndex - 1);
    }
  }

  /** Prefer {@link String#startsWith(String, int)} over less efficient alternatives. */
  static final class StringStartsWith {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    boolean before(String string, String prefix, int fromIndex) {
      return string.substring(fromIndex).startsWith(prefix);
    }

    @AfterTemplate
    boolean after(String string, String prefix, int fromIndex) {
      return string.startsWith(prefix, fromIndex);
    }
  }

  /**
   * Prefer {@link String#formatted(Object...)} over {@link String#format(String, Object...)}, as
   * the former works more nicely with text blocks, while the latter does not appear advantageous in
   * any circumstance (assuming one targets JDK 15+).
   */
  static final class StringFormatted {
    @BeforeTemplate
    @FormatMethod
    String before(String format, @Repeated Object args) {
      return String.format(format, args);
    }

    // XXX: Drop the unnecessary parentheses once Refaster automatically wraps string
    // concatenations. See https://github.com/google/error-prone/issues/4866.
    @AfterTemplate
    @FormatMethod
    @SuppressWarnings("UnnecessaryParentheses" /* Parentheses compensate for a Refaster bug. */)
    String after(String format, @Repeated Object args) {
      return (format).formatted(args);
    }
  }
}
