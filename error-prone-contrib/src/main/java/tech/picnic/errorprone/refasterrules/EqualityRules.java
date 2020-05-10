package tech.picnic.errorprone.refasterrules;

import static java.util.function.Predicate.not;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.checkerframework.checker.interning.qual.Interned;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with (in)equalities. */
@OnlineDocumentation
final class EqualityRules {
  private EqualityRules() {}

  /** Prefer reference-based quality for enums. */
  // Primitive value comparisons are not listed, because Error Prone flags those out of the box.
  static final class PrimitiveOrReferenceEquality<T extends @Interned Enum<T>> {
    /**
     * Enums can be compared by reference. It is safe to do so even in the face of refactorings,
     * because if the type is ever converted to a non-enum, then Error-Prone will complain about any
     * remaining reference-based equality checks.
     */
    // XXX: This Refaster rule is the topic of https://github.com/google/error-prone/issues/559. We
    // work around the issue by selecting the "largest replacements". See the `Refaster` check.
    @BeforeTemplate
    @SuppressWarnings("interning:unnecessary.equals" /* This violation will be rewritten. */)
    boolean before(T a, T b) {
      return Refaster.anyOf(a.equals(b), Objects.equals(a, b));
    }

    @AfterTemplate
    @AlsoNegation
    @SuppressWarnings("java:S1698" /* Reference comparison is valid for enums. */)
    boolean after(T a, T b) {
      return a == b;
    }
  }

  /** Prefer {@link Object#equals(Object)} over the equivalent lambda function. */
  // XXX: As it stands, this rule is a special case of what `MethodReferenceUsage` tries to achieve.
  // If/when `MethodReferenceUsage` becomes production ready, we should simply drop this check.
  // XXX: Alternatively, the rule should be replaced with a plugin that also identifies cases where
  // the arguments are swapped but simplification is possible anyway, by virtue of `v` being
  // non-null.
  static final class EqualsPredicate<T> {
    @BeforeTemplate
    Predicate<T> before(T v) {
      return e -> v.equals(e);
    }

    @AfterTemplate
    Predicate<T> after(T v) {
      return v::equals;
    }
  }

  /** Avoid double negations; this is not Javascript. */
  static final class DoubleNegation {
    @BeforeTemplate
    @SuppressWarnings("java:S2761" /* This violation will be rewritten. */)
    boolean before(boolean b) {
      return !!b;
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    boolean after(boolean b) {
      return b;
    }
  }

  /**
   * Don't negate an equality test or use the ternary operator to compare two booleans; directly
   * test for inequality instead.
   */
  // XXX: Replacing `a ? !b : b` with `a != b` changes semantics if both `a` and `b` are boxed
  // booleans.
  @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
  static final class Negation {
    @BeforeTemplate
    boolean before(boolean a, boolean b) {
      return Refaster.anyOf(!(a == b), a ? !b : b);
    }

    @BeforeTemplate
    @SuppressWarnings(
        "java:S1244" /* The equality check is fragile, but may be seen in the wild. */)
    boolean before(double a, double b) {
      return !(a == b);
    }

    @BeforeTemplate
    boolean before(@Interned Object a, @Interned Object b) {
      return !(a == b);
    }

    @AfterTemplate
    boolean after(boolean a, boolean b) {
      return a != b;
    }
  }

  /**
   * Don't negate an inequality test or use the ternary operator to compare two booleans; directly
   * test for equality instead.
   */
  // XXX: Replacing `a ? b : !b` with `a == b` changes semantics if both `a` and `b` are boxed
  // booleans.
  @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
  static final class IndirectDoubleNegation {
    @BeforeTemplate
    boolean before(boolean a, boolean b) {
      return Refaster.anyOf(!(a != b), a ? b : !b);
    }

    @BeforeTemplate
    @SuppressWarnings(
        "java:S1244" /* The inequality check is fragile, but may be seen in the wild. */)
    boolean before(double a, double b) {
      return !(a != b);
    }

    @BeforeTemplate
    boolean before(@Interned Object a, @Interned Object b) {
      return !(a != b);
    }

    @AfterTemplate
    boolean after(boolean a, boolean b) {
      return a == b;
    }
  }

  /**
   * Don't pass a lambda expression to {@link Predicate#not(Predicate)}; instead push the negation
   * into the lambda expression.
   */
  abstract static class PredicateLambda<T> {
    @Placeholder(allowsIdentity = true)
    abstract boolean predicate(@MayOptionallyUse T value);

    @BeforeTemplate
    Predicate<T> before() {
      return not(v -> predicate(v));
    }

    @AfterTemplate
    Predicate<T> after() {
      return v -> !predicate(v);
    }
  }

  /** Avoid contrived ways of handling {@code null} values during equality testing. */
  static final class EqualsLhsNullable<T, S> {
    @BeforeTemplate
    boolean before(T value1, S value2) {
      return Optional.ofNullable(value1).equals(Optional.of(value2));
    }

    @AfterTemplate
    boolean after(T value1, S value2) {
      return value2.equals(value1);
    }
  }

  /** Avoid contrived ways of handling {@code null} values during equality testing. */
  static final class EqualsRhsNullable<T, S> {
    @BeforeTemplate
    boolean before(T value1, S value2) {
      return Optional.of(value1).equals(Optional.ofNullable(value2));
    }

    @AfterTemplate
    boolean after(T value1, S value2) {
      return value1.equals(value2);
    }
  }

  /** Avoid contrived ways of handling {@code null} values during equality testing. */
  static final class EqualsLhsAndRhsNullable<T, S> {
    @BeforeTemplate
    boolean before(T value1, S value2) {
      return Optional.ofNullable(value1).equals(Optional.ofNullable(value2));
    }

    @AfterTemplate
    boolean after(T value1, S value2) {
      return Objects.equals(value1, value2);
    }
  }
}
