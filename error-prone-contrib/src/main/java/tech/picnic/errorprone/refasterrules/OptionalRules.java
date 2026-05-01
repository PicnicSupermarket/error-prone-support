package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.RequiresComputation;

/** Refaster rules related to expressions dealing with {@link Optional}s. */
@OnlineDocumentation
final class OptionalRules {
  private OptionalRules() {}

  /** Prefer {@link Optional#empty()} over more contrived alternatives. */
  static final class OptionalEmpty<T> {
    @BeforeTemplate
    Optional<T> before() {
      return Optional.ofNullable(null);
    }

    @AfterTemplate
    Optional<T> after() {
      return Optional.empty();
    }
  }

  /** Prefer {@link Optional#ofNullable(Object)} over more contrived alternatives. */
  static final class OptionalOfNullable<T> {
    // XXX: Refaster should be smart enough to also rewrite occurrences in which there are
    // parentheses around the null check, but that's currently not the case. Try to fix that.
    @BeforeTemplate
    @SuppressWarnings("RefasterEmitCommentBeforeOptionalOfFilterNot" /* Special case. */)
    Optional<T> before(@Nullable T value) {
      return value == null ? Optional.empty() : Optional.of(value);
    }

    @AfterTemplate
    Optional<T> after(T value) {
      return Optional.ofNullable(value);
    }
  }

  /** Prefer {@link Optional#isEmpty()} over more verbose alternatives. */
  static final class OptionalIsEmpty<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isPresent();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isEmpty();
    }
  }

  /** Prefer {@link Optional#isPresent()} over more verbose alternatives. */
  static final class OptionalIsPresent<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isEmpty();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isPresent();
    }
  }

  /** Prefer {@link Optional#orElseThrow()} over the less explicit {@link Optional#get()}. */
  static final class OptionalOrElseThrowWithOptional<T> {
    @BeforeTemplate
    @SuppressWarnings({
      "java:S3655" /* Matched expressions are in practice embedded in a larger context. */,
      "NullAway" /* Matched expressions are in practice embedded in a larger context. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    T before(Optional<T> optional) {
      return optional.get();
    }

    @AfterTemplate
    T after(Optional<T> optional) {
      return optional.orElseThrow();
    }
  }

  /** Prefer {@link Optional#orElseThrow()} over the less explicit {@link Optional#get()}. */
  // XXX: This rule is analogous to `OptionalOrElseThrowWithOptional` above. Arguably this is its
  // generalization. If/when Refaster is extended to understand this, delete the rule above.
  static final class OptionalOrElseThrow<T> {
    @BeforeTemplate
    Function<Optional<T>, T> before() {
      return Optional::get;
    }

    @AfterTemplate
    Function<Optional<T>, T> after() {
      return Optional::orElseThrow;
    }
  }

  /** Prefer {@link Optional#equals(Object)} over more contrived alternatives. */
  static final class OptionalEqualsOptionalOf<T, S> {
    @BeforeTemplate
    boolean before(Optional<T> optional, S value) {
      return Refaster.anyOf(
          optional.filter(value::equals).isPresent(), optional.stream().anyMatch(value::equals));
    }

    @AfterTemplate
    boolean after(Optional<T> optional, S value) {
      return optional.equals(Optional.of(value));
    }
  }

  /** Prefer {@code Streams.stream(iterator).findFirst()} over more contrived alternatives. */
  static final class StreamsStreamFindFirst<T> {
    @BeforeTemplate
    Optional<T> before(Iterator<T> iterator) {
      return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Optional<T> after(Iterator<T> iterator) {
      return Streams.stream(iterator).findFirst();
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Review whether a `@Matcher` can be used to avoid this.
  abstract static class RefasterEmitCommentBeforeOptionalOfFilter<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T value) {
      return test(value) ? Optional.of(value) : Optional.empty();
    }

    @AfterTemplate
    Optional<T> after(T value) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(value).filter(v -> test(v)));
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Review whether a `@Matcher` can be used to avoid this.
  abstract static class RefasterEmitCommentBeforeOptionalOfFilterNot<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T value) {
      return test(value) ? Optional.empty() : Optional.of(value);
    }

    @AfterTemplate
    Optional<T> after(T value) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(value).filter(v -> !test(v)));
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over more contrived alternatives. */
  static final class OptionalFilterIsPresent<S, T extends S> {
    @BeforeTemplate
    Boolean before(Optional<T> optional, Function<S, Boolean> predicate) {
      return optional.map(predicate).orElse(false);
    }

    @AfterTemplate
    boolean after(Optional<T> optional, Predicate<S> predicate) {
      return optional.filter(predicate).isPresent();
    }
  }

  /** Prefer {@link Optional#map(Function)} over more contrived alternatives. */
  abstract static class OptionalMap<T, S> {
    @Placeholder
    abstract S toNullableFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<S> before(Optional<T> optional) {
      return optional.flatMap(
          v ->
              Refaster.anyOf(
                  Optional.of(toNullableFunction(v)), Optional.ofNullable(toNullableFunction(v))));
    }

    @AfterTemplate
    Optional<S> after(Optional<T> optional) {
      return optional.map(v -> toNullableFunction(v));
    }
  }

  /** Prefer {@link Optional#flatMap(Function)} over more contrived alternatives. */
  abstract static class OptionalFlatMap<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<S> before(Optional<T> optional) {
      return optional.map(v -> toOptionalFunction(v).orElseThrow());
    }

    @AfterTemplate
    Optional<S> after(Optional<T> optional) {
      return optional.flatMap(v -> toOptionalFunction(v));
    }
  }

  /**
   * Prefer {@link Optional#or(Supplier)} combined with {@link Optional#orElseThrow()} over more
   * contrived alternatives.
   */
  static final class OptionalOrOrElseThrow<T> {
    @BeforeTemplate
    T before(Optional<T> optional1, Optional<T> optional2) {
      return optional1.orElseGet(() -> optional2.orElseThrow());
    }

    @AfterTemplate
    T after(Optional<T> optional1, Optional<T> optional2) {
      return optional1.or(() -> optional2).orElseThrow();
    }
  }

  /** Prefer {@link Optional#orElse(Object)} over more contrived alternatives. */
  // XXX: This rule is the counterpart to the `OptionalOrElseGet` bug checker. Once the
  // `MethodReferenceUsage` bug checker is "production ready", that bug checker may similarly be
  // replaced with a Refaster rule.
  static final class OptionalOrElse<T> {
    @BeforeTemplate
    T before(Optional<T> optional, @NotMatches(RequiresComputation.class) T other) {
      return optional.orElseGet(() -> other);
    }

    @AfterTemplate
    T after(Optional<T> optional, T other) {
      return optional.orElse(other);
    }
  }

  /** Prefer {@link Optional#stream()} over more verbose or non-JDK alternatives. */
  // XXX: Do we need the `.filter(Optional::isPresent)`? If it's absent the caller probably assumed
  // that the values are present. (If we drop it, we should rewrite vacuous filter steps.)
  // XXX: The rewritten `filter`/`map` expression may be more performant than its replacement. See
  // https://github.com/palantir/gradle-baseline/pull/2946. (There are plans to pair Refaster rules
  // with JMH benchmarks; this would be a great use case.)
  // XXX: Perhaps `stream.mapMulti(Optional::ifPresent)` is what we should use. See
  // https://github.com/palantir/gradle-baseline/pull/2996.
  static final class StreamFlatMapOptionalStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<Optional<T>> stream) {
      return Refaster.anyOf(
          stream.filter(Optional::isPresent).map(Optional::orElseThrow),
          stream.flatMap(Streams::stream));
    }

    @AfterTemplate
    Stream<T> after(Stream<Optional<T>> stream) {
      return stream.flatMap(Optional::stream);
    }
  }

  /**
   * Prefer {@link Optional#stream()} within {@link Stream#flatMap(Function)} over more contrived
   * alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving. The
   * original code throws an exception if the mapping operation does not produce a value, while the
   * replacement does not.
   */
  // XXX: An alternative approach is to use `.flatMap(Optional::stream)`. That may be a bit longer,
  // but yields nicer code. Think about it.
  abstract static class StreamFlatMapStream<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream) {
      return stream.map(e -> toOptionalFunction(e).orElseThrow());
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream) {
      return stream.flatMap(e -> toOptionalFunction(e).stream());
    }
  }

  /**
   * Prefer {@link Optional#filter(Predicate)} outside of {@link Optional#flatMap(Function)} over
   * more contrived alternatives.
   */
  abstract static class OptionalFlatMapFilter<T, W, S extends W> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<S> before(Optional<T> optional, Predicate<W> predicate) {
      return optional.flatMap(v -> toOptionalFunction(v).filter(predicate));
    }

    @AfterTemplate
    Optional<S> after(Optional<T> optional, Predicate<W> predicate) {
      return optional.flatMap(v -> toOptionalFunction(v)).filter(predicate);
    }
  }

  /**
   * Prefer {@link Optional#map(Function)} outside of {@link Optional#flatMap(Function)} over more
   * contrived alternatives.
   */
  abstract static class OptionalFlatMapMap<T, U, S extends U, R, V extends R> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<V> before(Optional<T> optional, Function<U, V> mapper) {
      return optional.flatMap(v -> toOptionalFunction(v).map(mapper));
    }

    @AfterTemplate
    Optional<V> after(Optional<T> optional, Function<U, V> mapper) {
      return optional.flatMap(v -> toOptionalFunction(v)).map(mapper);
    }
  }

  /** Avoid unnecessary nesting of {@link Optional#flatMap(Function)} operations. */
  abstract static class OptionalFlatMapFlatMap<T, S, R> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<R> before(Optional<T> optional, Function<? super S, Optional<? extends R>> mapper) {
      return optional.flatMap(v -> toOptionalFunction(v).flatMap(mapper));
    }

    @AfterTemplate
    Optional<R> after(Optional<T> optional, Function<? super S, Optional<? extends R>> mapper) {
      return optional.flatMap(v -> toOptionalFunction(v)).flatMap(mapper);
    }
  }

  /** Prefer {@link Optional#or(Supplier)} over more verbose alternatives. */
  static final class OptionalOr<T> {
    @BeforeTemplate
    @SuppressWarnings({
      "LexicographicalAnnotationAttributeListing" /* `key-*` entry must remain last. */,
      "NestedOptionals" /* This violation will be rewritten. */,
      "OptionalOrElse" /* Parameters represent expressions that may require computation. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    Optional<T> before(Optional<T> optional, Optional<T> other) {
      // XXX: Note that rewriting the first and third variant will change the code's behavior if
      // `optional2` has side-effects.
      // XXX: Note that rewriting the first, third and fourth variant will introduce a compilation
      // error if `optional2` is not effectively final. Review whether a `@Matcher` can be used to
      // avoid this.
      return Refaster.anyOf(
          optional.map(Optional::of).orElse(other),
          optional.map(Optional::of).orElseGet(() -> other),
          Stream.of(optional, other).flatMap(Optional::stream).findFirst(),
          optional.isPresent() ? optional : other);
    }

    @AfterTemplate
    Optional<T> after(Optional<T> optional, Optional<T> other) {
      return optional.or(() -> other);
    }
  }

  /** Prefer using {@link Optional}s as-is over more contrived alternatives. */
  static final class OptionalIdentity<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("NestedOptionals")
    Optional<T> before(Optional<T> optional, Comparator<S> cmp) {
      return Refaster.anyOf(
          optional.or(Refaster.anyOf(() -> Optional.empty(), Optional::empty)),
          optional
              .map(Optional::of)
              .orElseGet(Refaster.anyOf(() -> Optional.empty(), Optional::empty)),
          optional.stream().findFirst(),
          optional.stream().findAny(),
          optional.stream().min(cmp),
          optional.stream().max(cmp));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    Optional<T> after(Optional<T> optional) {
      return optional;
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over more contrived alternatives. */
  static final class OptionalFilter<S, T extends S> {
    @BeforeTemplate
    Optional<T> before(Optional<T> optional, Predicate<S> predicate) {
      return Refaster.anyOf(
          optional.stream().filter(predicate).findFirst(),
          optional.stream().filter(predicate).findAny());
    }

    @AfterTemplate
    Optional<T> after(Optional<T> optional, Predicate<S> predicate) {
      return optional.filter(predicate);
    }
  }

  /** Prefer {@link Optional#map(Function)} over more contrived alternatives. */
  // XXX: If `StreamFindFirstMap` also simplifies `.findAny()` expressions, then this rule can be
  // dropped in favour of `StreamMapFirst` and `OptionalIdentity`.
  static final class OptionalMapWithFunction<W, S extends W, T, V extends T> {
    @BeforeTemplate
    Optional<V> before(Optional<S> optional, Function<W, V> mapper) {
      return optional.stream().map(mapper).findAny();
    }

    @AfterTemplate
    Optional<V> after(Optional<S> optional, Function<W, V> mapper) {
      return optional.map(mapper);
    }
  }

  /** Prefer {@link Optional#stream()} over more contrived alternatives. */
  static final class OptionalStream<T> {
    @BeforeTemplate
    Stream<T> before(Optional<T> optional) {
      return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    @AfterTemplate
    Stream<T> after(Optional<T> optional) {
      return optional.stream();
    }
  }

  // XXX: Add a rule for:
  // `optional.flatMap(x -> pred(x) ? Optional.empty() : Optional.of(x))` and variants.
  // (Maybe canonicalize the inner expression. Maybe we rewrite it already.)
}
