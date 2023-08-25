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
import tech.picnic.errorprone.refaster.matchers.IsLikelyTrivialComputation;

/** Refaster rules related to expressions dealing with {@link Optional}s. */
@OnlineDocumentation
final class OptionalRules {
  private OptionalRules() {}

  static final class OptionalOfNullable<T> {
    // XXX: Refaster should be smart enough to also rewrite occurrences in which there are
    // parentheses around the null check, but that's currently not the case. Try to fix that.
    @BeforeTemplate
    @SuppressWarnings("TernaryOperatorOptionalNegativeFiltering" /* Special case. */)
    Optional<T> before(@Nullable T object) {
      return object == null ? Optional.empty() : Optional.of(object);
    }

    @AfterTemplate
    Optional<T> after(T object) {
      return Optional.ofNullable(object);
    }
  }

  /** Prefer {@link Optional#isEmpty()} over the more verbose alternative. */
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

  /** Prefer {@link Optional#isPresent()} over the inverted alternative. */
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
  static final class OptionalOrElseThrow<T> {
    @BeforeTemplate
    @SuppressWarnings({
      "java:S3655" /* Matched expressions are in practice embedded in a larger context. */,
      "NullAway"
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
  // XXX: This rule is analogous to `OptionalOrElseThrow` above. Arguably this is its
  // generalization. If/when Refaster is extended to understand this, delete the rule above.
  static final class OptionalOrElseThrowMethodReference<T> {
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
  static final class OptionalHasValue<T, S> {
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

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Iterator} as an {@link Optional}.
   */
  static final class OptionalFirstIteratorElement<T> {
    @BeforeTemplate
    Optional<T> before(Iterator<T> it) {
      return it.hasNext() ? Optional.of(it.next()) : Optional.empty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Optional<T> after(Iterator<T> it) {
      return Streams.stream(it).findFirst();
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Review whether a `@Matcher` can be used to avoid this.
  abstract static class TernaryOperatorOptionalPositiveFiltering<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T input) {
      return test(input) ? Optional.of(input) : Optional.empty();
    }

    @AfterTemplate
    Optional<T> after(T input) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(input).filter(v -> test(v)));
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Review whether a `@Matcher` can be used to avoid this.
  abstract static class TernaryOperatorOptionalNegativeFiltering<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T input) {
      return test(input) ? Optional.empty() : Optional.of(input);
    }

    @AfterTemplate
    Optional<T> after(T input) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(input).filter(v -> !test(v)));
    }
  }

  /**
   * Prefer {@link Optional#filter(Predicate)} over {@link Optional#map(Function)} when converting
   * an {@link Optional} to a boolean.
   */
  static final class MapOptionalToBoolean<T> {
    @BeforeTemplate
    @SuppressWarnings("OptionalOrElseGet" /* Rule is confused by `Refaster#anyOf` usage. */)
    boolean before(Optional<T> optional, Function<? super T, Boolean> predicate) {
      return optional.map(predicate).orElse(Refaster.anyOf(false, Boolean.FALSE));
    }

    @AfterTemplate
    boolean after(Optional<T> optional, Predicate<? super T> predicate) {
      return optional.filter(predicate).isPresent();
    }
  }

  /**
   * Prefer {@link Optional#map} over a {@link Optional#flatMap} that wraps the result of a
   * transformation in an {@link Optional}; the former operation transforms {@code null} to {@link
   * Optional#empty()}.
   */
  abstract static class MapToNullable<T, S> {
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

  abstract static class FlatMapToOptional<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    @SuppressWarnings("NullAway")
    Optional<S> before(Optional<T> optional) {
      return optional.map(v -> toOptionalFunction(v).orElseThrow());
    }

    @AfterTemplate
    Optional<S> after(Optional<T> optional) {
      return optional.flatMap(v -> toOptionalFunction(v));
    }
  }

  static final class OrOrElseThrow<T> {
    @BeforeTemplate
    @SuppressWarnings("NullAway")
    T before(Optional<T> o1, Optional<T> o2) {
      return o1.orElseGet(() -> o2.orElseThrow());
    }

    @AfterTemplate
    @SuppressWarnings("NullAway")
    T after(Optional<T> o1, Optional<T> o2) {
      return o1.or(() -> o2).orElseThrow();
    }
  }

  /**
   * Prefer {@link Optional#orElseGet(Supplier)} over {@link Optional#orElse(Object)} if the
   * fallback value is not the result of a trivial computation.
   */
  // XXX: This rule may introduce a compilation error: the `value` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Review whether a `@Matcher` can be used to avoid this.
  // XXX: Once `MethodReferenceUsage` is "production ready", replace
  // `@NotMatches(IsLikelyTrivialComputation.class)` with `@Matches(RequiresComputation.class)` (and
  // reimplement the matcher accordingly).
  static final class OptionalOrElseGet<T> {
    @BeforeTemplate
    T before(Optional<T> optional, @NotMatches(IsLikelyTrivialComputation.class) T value) {
      return optional.orElse(value);
    }

    @AfterTemplate
    T after(Optional<T> optional, T value) {
      return optional.orElseGet(() -> value);
    }
  }

  /**
   * Flatten a stream of {@link Optional}s using {@link Optional#stream()}, rather than using one of
   * the more verbose alternatives.
   */
  // XXX: Do we need the `.filter(Optional::isPresent)`? If it's absent the caller probably assumed
  // that the values are present. (If we drop it, we should rewrite vacuous filter steps.)
  static final class StreamFlatMapOptional<T> {
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
   * Within a stream's map operation unconditional {@link Optional#orElseThrow()} calls can be
   * avoided.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving. The
   * original code throws an exception if the mapping operation does not produce a value, while the
   * replacement does not.
   */
  // XXX: An alternative approach is to use `.flatMap(Optional::stream)`. That may be a bit longer,
  // but yields nicer code. Think about it.
  abstract static class StreamMapToOptionalGet<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    @SuppressWarnings("NullAway")
    Stream<S> before(Stream<T> stream) {
      return stream.map(e -> toOptionalFunction(e).orElseThrow());
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream) {
      return stream.flatMap(e -> toOptionalFunction(e).stream());
    }
  }

  /** Avoid unnecessary nesting of {@link Optional#filter(Predicate)} operations. */
  abstract static class FilterOuterOptionalAfterFlatMap<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<S> before(Optional<T> optional, Predicate<? super S> predicate) {
      return optional.flatMap(v -> toOptionalFunction(v).filter(predicate));
    }

    @AfterTemplate
    Optional<S> after(Optional<T> optional, Predicate<? super S> predicate) {
      return optional.flatMap(v -> toOptionalFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link Optional#map(Function)} operations. */
  abstract static class MapOuterOptionalAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<R> before(Optional<T> optional, Function<? super S, ? extends R> function) {
      return optional.flatMap(v -> toOptionalFunction(v).map(function));
    }

    @AfterTemplate
    Optional<R> after(Optional<T> optional, Function<? super S, ? extends R> function) {
      return optional.flatMap(v -> toOptionalFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link Optional#flatMap(Function)} operations. */
  abstract static class FlatMapOuterOptionalAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Optional<R> before(Optional<T> optional, Function<? super S, Optional<? extends R>> function) {
      return optional.flatMap(v -> toOptionalFunction(v).flatMap(function));
    }

    @AfterTemplate
    Optional<R> after(Optional<T> optional, Function<? super S, Optional<? extends R>> function) {
      return optional.flatMap(v -> toOptionalFunction(v)).flatMap(function);
    }
  }

  /** Prefer {@link Optional#or(Supplier)} over more verbose alternatives. */
  static final class OptionalOrOtherOptional<T> {
    @BeforeTemplate
    @SuppressWarnings("NestedOptionals" /* Auto-fix for the `NestedOptionals` check. */)
    Optional<T> before(Optional<T> optional1, Optional<T> optional2) {
      // XXX: Note that rewriting the first and third variant will change the code's behavior if
      // `optional2` has side-effects.
      // XXX: Note that rewriting the first, third and fourth variant will introduce a compilation
      // error if `optional2` is not effectively final. Review whether a `@Matcher` can be used to
      // avoid this.
      return Refaster.anyOf(
          optional1.map(Optional::of).orElse(optional2),
          optional1.map(Optional::of).orElseGet(() -> optional2),
          Stream.of(optional1, optional2).flatMap(Optional::stream).findFirst(),
          optional1.isPresent() ? optional1 : optional2);
    }

    @AfterTemplate
    Optional<T> after(Optional<T> optional1, Optional<T> optional2) {
      return optional1.or(() -> optional2);
    }
  }

  /**
   * Avoid unnecessary operations on an {@link Optional} that ultimately result in that very same
   * {@link Optional}.
   */
  static final class OptionalIdentity<T> {
    @BeforeTemplate
    Optional<T> before(Optional<T> optional, Comparator<? super T> comparator) {
      return Refaster.anyOf(
          optional.stream().findFirst(),
          optional.stream().findAny(),
          optional.stream().min(comparator),
          optional.stream().max(comparator));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    Optional<T> after(Optional<T> optional) {
      return optional;
    }
  }

  /**
   * Avoid unnecessary {@link Optional} to {@link Stream} conversion when filtering a value of the
   * former type.
   */
  static final class OptionalFilter<T> {
    @BeforeTemplate
    Optional<T> before(Optional<T> optional, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          optional.stream().filter(predicate).findFirst(),
          optional.stream().filter(predicate).findAny());
    }

    @AfterTemplate
    Optional<T> after(Optional<T> optional, Predicate<? super T> predicate) {
      return optional.filter(predicate);
    }
  }

  /**
   * Avoid unnecessary {@link Optional} to {@link Stream} conversion when mapping a value of the
   * former type.
   */
  // XXX: If `StreamMapFirst` also simplifies `.findAny()` expressions, then this rule can be
  // dropped in favour of `StreamMapFirst` and `OptionalIdentity`.
  static final class OptionalMap<S, T> {
    @BeforeTemplate
    Optional<? extends T> before(Optional<S> optional, Function<? super S, ? extends T> function) {
      return optional.stream().map(function).findAny();
    }

    @AfterTemplate
    Optional<? extends T> after(Optional<S> optional, Function<? super S, ? extends T> function) {
      return optional.map(function);
    }
  }

  /** Prefer {@link Optional#stream()} over more contrived alternatives. */
  static final class OptionalStream<T> {
    @BeforeTemplate
    Stream<T> before(Optional<T> optional) {
      return Refaster.anyOf(
          optional.map(Stream::of).orElse(Stream.empty()),
          optional.map(Stream::of).orElseGet(Stream::empty));
    }

    @AfterTemplate
    Stream<T> after(Optional<T> optional) {
      return optional.stream();
    }
  }

  // XXX: Add a rule for:
  // `optional.flatMap(x -> pred(x) ? Optional.empty() : Optional.of(x))` and variants.
  // (Maybe canonicalize the inner expression. Maybe we rewrite already.)
}
