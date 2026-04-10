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

  /** Prefer {@code ""} over less efficient or less explicit alternatives. */
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

  /** Prefer using {@link String}s as-is over less efficient alternatives. */
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

  /** Prefer {@link String#isEmpty()} over less explicit alternatives. */
  // XXX: Drop this rule once we (and OpenRewrite) no longer support projects targeting Java 14 or
  // below. The `CharSequenceIsEmpty` rule then suffices. (This rule exists so that e.g. projects
  // that target JDK 11 can disable `CharSequenceIsEmpty` without losing a valuable rule.)
  // XXX: Look into a more general approach to supporting different Java language levels, such as
  // rule selection based on some annotation, or a separate Maven module.
  static final class StringIsEmptyWithString {
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

  /** Prefer {@link String#isEmpty()} over more verbose alternatives. */
  // XXX: Now that we build with JDK 15+, this rule can be generalized to cover all `CharSequence`
  // subtypes. However, `CharSequence::isEmpty` isn't as nice as `String::isEmpty`, so we might want
  // to introduce a rule that suggests `String::isEmpty` where possible.
  // XXX: As it stands, this rule is a special case of what `MethodReferenceUsage` tries to achieve.
  // If/when `MethodReferenceUsage` becomes production ready, we should simply drop this check.
  static final class StringIsEmpty {
    @BeforeTemplate
    Predicate<String> before() {
      return s -> s.isEmpty();
    }

    @AfterTemplate
    Predicate<String> after() {
      return String::isEmpty;
    }
  }

  /** Prefer {@code not(String::isEmpty)} over less idiomatic alternatives. */
  // XXX: Now that we build with JDK 15+, this rule can be generalized to cover all `CharSequence`
  // subtypes. However, `CharSequence::isEmpty` isn't as nice as `String::isEmpty`, so we might want
  // to introduce a rule that suggests `String::isEmpty` where possible.
  static final class NotStringIsEmpty {
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

  /** Prefer {@link Strings#isNullOrEmpty(String)} over more verbose alternatives. */
  static final class StringsIsNullOrEmpty {
    @BeforeTemplate
    boolean before(@Nullable String string) {
      return string == null || string.isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(String string) {
      return Strings.isNullOrEmpty(string);
    }
  }

  /**
   * Prefer {@link String#isBlank()} over less efficient alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes the behavior for strings containing
   * whitespace characters beyond U+0020, as {@link String#isBlank()} considers those, while {@link
   * String#trim()} does not.
   */
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

  /**
   * Prefer {@code Optional.ofNullable(str).filter(not(String::isEmpty))} over more contrived
   * alternatives.
   */
  // XXX: This is a special case of `RefasterEmitCommentBeforeOptionalOfFilterNot`.
  static final class OptionalOfNullableFilterNotStringIsEmpty {
    @BeforeTemplate
    Optional<String> before(String value) {
      return Strings.isNullOrEmpty(value)
          ? Optional.empty()
          : Refaster.anyOf(Optional.of(value), Optional.ofNullable(value));
    }

    @AfterTemplate
    Optional<String> after(String value) {
      return Optional.ofNullable(value).filter(not(String::isEmpty));
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over non-JDK alternatives. */
  static final class OptionalFilterNotStringIsEmpty {
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

  /**
   * Prefer {@link String#join(CharSequence, Iterable)} over non-JDK or more contrived alternatives.
   */
  // XXX: Joiner.on(char) isn't rewritten. Add separate rule?
  // XXX: Joiner#join(@Nullable Object first, @Nullable Object second, Object... rest) isn't
  // rewritten.
  static final class StringJoin<T extends CharSequence> {
    @BeforeTemplate
    String before(String delimiter, T[] elements) {
      return Refaster.anyOf(
          Joiner.on(delimiter).join(elements), Arrays.stream(elements).collect(joining(delimiter)));
    }

    @BeforeTemplate
    String before(String delimiter, Iterable<T> elements) {
      return Refaster.anyOf(
          Joiner.on(delimiter).join(elements),
          Streams.stream(elements).collect(joining(delimiter)));
    }

    @BeforeTemplate
    String before(CharSequence delimiter, Collection<T> elements) {
      return elements.stream().collect(joining(delimiter));
    }

    @AfterTemplate
    String after(CharSequence delimiter, Iterable<T> elements) {
      return String.join(delimiter, elements);
    }
  }

  /** Prefer {@link String#join(CharSequence, CharSequence...)} over less efficient alternatives. */
  static final class StringJoinVarargs {
    @BeforeTemplate
    String before(CharSequence delimiter, @Repeated CharSequence elements) {
      return Stream.of(Refaster.asVarargs(elements)).collect(joining(delimiter));
    }

    @AfterTemplate
    String after(CharSequence delimiter, @Repeated CharSequence elements) {
      return String.join(delimiter, Refaster.asVarargs(elements));
    }
  }

  /** Prefer {@link String#valueOf(Object)} over more contrived alternatives. */
  static final class StringValueOfWithObject {
    @BeforeTemplate
    String before(Object obj) {
      return Objects.toString(obj);
    }

    @AfterTemplate
    String after(Object obj) {
      return String.valueOf(obj);
    }
  }

  /** Prefer {@link String#String(char[], int, int)} over more contrived alternatives. */
  static final class NewString3 {
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

  /** Prefer {@link String#String(char[])} over more contrived alternatives. */
  static final class NewString1 {
    @BeforeTemplate
    String before(char[] data) {
      return Refaster.anyOf(String.valueOf(data), new String(data, 0, data.length));
    }

    @AfterTemplate
    String after(char[] data) {
      return new String(data);
    }
  }

  /** Prefer {@link String#valueOf(Object)} over more contrived alternatives. */
  // XXX: This rule is analogous to `StringValueOf` above. Arguably this is its generalization.
  // If/when Refaster is extended to understand this, delete the rule above.
  static final class StringValueOf {
    @BeforeTemplate
    Function<Object, String> before() {
      return Objects::toString;
    }

    @AfterTemplate
    Function<Object, String> after() {
      return String::valueOf;
    }
  }

  /** Prefer {@link String#substring(int)} over more verbose alternatives. */
  static final class StringSubstring {
    @BeforeTemplate
    String before(String str, int beginIndex) {
      return str.substring(beginIndex, str.length());
    }

    @AfterTemplate
    String after(String str, int beginIndex) {
      return str.substring(beginIndex);
    }
  }

  /** Prefer {@link Utf8#encodedLength(CharSequence)} over less efficient alternatives. */
  static final class Utf8EncodedLength {
    @BeforeTemplate
    int before(String sequence) {
      return sequence.getBytes(UTF_8).length;
    }

    @AfterTemplate
    int after(String sequence) {
      return Utf8.encodedLength(sequence);
    }
  }

  /** Prefer {@link String#indexOf(int, int)} over less efficient alternatives. */
  static final class MathMaxNegativeOneStringIndexOfMinusInt {
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
  static final class MathMaxNegativeOneStringIndexOfMinusIntWithInt {
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
  static final class MathMaxNegativeOneStringIndexOfMinusString {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, String str, int fromIndex) {
      return string.substring(fromIndex).indexOf(str);
    }

    @AfterTemplate
    int after(String string, String str, int fromIndex) {
      return Math.max(-1, string.indexOf(str, fromIndex) - fromIndex);
    }
  }

  /** Prefer {@link String#indexOf(String, int)} over less efficient alternatives. */
  static final class MathMaxNegativeOneStringIndexOfMinusStringWithInt {
    @BeforeTemplate
    int before(String string, String str, int beginIndex, int endIndex) {
      return string.substring(beginIndex, endIndex).indexOf(str);
    }

    @AfterTemplate
    int after(String string, String str, int beginIndex, int endIndex) {
      return Math.max(-1, string.indexOf(str, beginIndex, endIndex) - beginIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(int, int)} over less efficient alternatives. */
  static final class MathMaxNegativeOneStringLastIndexOfMinusInt {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, int ch, int beginIndex) {
      return string.substring(beginIndex).lastIndexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int beginIndex) {
      return Math.max(-1, string.lastIndexOf(ch) - beginIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(String, int)} over less efficient alternatives. */
  static final class MathMaxNegativeOneStringLastIndexOfMinusString {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    int before(String string, String str, int beginIndex) {
      return string.substring(beginIndex).lastIndexOf(str);
    }

    @AfterTemplate
    int after(String string, String str, int beginIndex) {
      return Math.max(-1, string.lastIndexOf(str) - beginIndex);
    }
  }

  /** Prefer {@link String#lastIndexOf(int, int)} over less efficient alternatives. */
  static final class StringLastIndexOfMinusOneInt {
    @BeforeTemplate
    int before(String string, int ch, int endIndex) {
      return string.substring(0, endIndex).lastIndexOf(ch);
    }

    @AfterTemplate
    int after(String string, int ch, int endIndex) {
      return string.lastIndexOf(ch, endIndex - 1);
    }
  }

  /**
   * Prefer {@link String#lastIndexOf(String, int)} over less efficient alternatives.
   *
   * <p><strong>Warning:</strong> when {@code substring} is empty, this rewrite changes the result:
   * the original expression returns {@code fromIndex}, while the replacement returns {@code
   * fromIndex - 1}.
   */
  static final class StringLastIndexOfMinusOneString {
    @BeforeTemplate
    int before(String string, String str, int endIndex) {
      return string.substring(0, endIndex).lastIndexOf(str);
    }

    @AfterTemplate
    int after(String string, String str, int endIndex) {
      return string.lastIndexOf(str, endIndex - 1);
    }
  }

  /** Prefer {@link String#startsWith(String, int)} over less efficient alternatives. */
  static final class StringStartsWith {
    @BeforeTemplate
    @SuppressWarnings("java:S4635" /* This violation will be rewritten. */)
    boolean before(String string, String prefix, int toffset) {
      return string.substring(toffset).startsWith(prefix);
    }

    @AfterTemplate
    boolean after(String string, String prefix, int toffset) {
      return string.startsWith(prefix, toffset);
    }
  }

  /** Prefer {@link String#formatted(Object...)} over less idiomatic alternatives. */
  static final class Formatted {
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
