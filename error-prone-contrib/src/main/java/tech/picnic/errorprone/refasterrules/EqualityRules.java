package tech.picnic.errorprone.refasterrules;

import static java.util.function.Predicate.isEqual;
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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with (in)equalities. */
@OnlineDocumentation
final class EqualityRules {
  private EqualityRules() {}

  /** Prefer enum {@code ==} comparison over less idiomatic alternatives. */
  // Primitive value comparisons are not matched, because Error Prone flags those out of the box.
  static final class EqualToWithEnum<T extends Enum<T>> {
    /**
     * Enums can be compared by reference. It is safe to do so even in the face of refactorings,
     * because if the type is ever converted to a non-enum, then Error-Prone will complain about any
     * remaining reference-based equality checks.
     */
    // XXX: This Refaster rule is the topic of https://github.com/google/error-prone/issues/559. We
    // work around the issue by selecting the "largest replacements". See the `Refaster` check.
    @BeforeTemplate
    @SuppressWarnings("EnumOrdinal" /* This violation will be rewritten. */)
    boolean before(T a, T other) {
      return Refaster.anyOf(
          a.equals(other), Objects.equals(a, other), a.ordinal() == other.ordinal());
    }

    @AfterTemplate
    @AlsoNegation
    @SuppressWarnings("java:S1698" /* Reference comparison is valid for enums. */)
    boolean after(T a, T other) {
      return a == other;
    }
  }

  /** Prefer enum {@code ==} comparison over less idiomatic alternatives. */
  static final class EqualTo<T extends Enum<T>> {
    @BeforeTemplate
    Predicate<T> before(T targetRef) {
      return Refaster.anyOf(isEqual(targetRef), targetRef::equals);
    }

    @AfterTemplate
    @SuppressWarnings("java:S1698" /* Reference comparison is valid for enums. */)
    Predicate<T> after(T targetRef) {
      return v -> v == targetRef;
    }
  }

  /** Prefer {@link Object#equals(Object)} method references over more verbose alternatives. */
  // XXX: As it stands, this rule is a special case of what `MethodReferenceUsage` tries to achieve.
  // If/when `MethodReferenceUsage` becomes production ready, we should simply drop this check.
  // XXX: Alternatively, the rule should be replaced with a plugin that also identifies cases where
  // the arguments are swapped but simplification is possible anyway, by virtue of `v` being
  // non-null.
  static final class TEquals<T> {
    @BeforeTemplate
    Predicate<T> before(T v) {
      return e -> v.equals(e);
    }

    @AfterTemplate
    Predicate<T> after(T v) {
      return v::equals;
    }
  }

  /** Prefer using the boolean expression as-is over more contrived alternatives. */
  static final class BooleanIdentity {
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

  /** Prefer {@code !=} over more contrived alternatives. */
  // XXX: Replacing `a ? !b : b` with `a != b` changes semantics if both `a` and `b` are boxed
  // booleans.
  @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
  static final class NotEqualTo {
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
    boolean before(Object a, Object b) {
      return !(a == b);
    }

    @AfterTemplate
    boolean after(boolean a, boolean b) {
      return a != b;
    }
  }

  /** Prefer {@code ==} over more contrived alternatives. */
  // XXX: Replacing `a ? b : !b` with `a == b` changes semantics if both `a` and `b` are boxed
  // booleans.
  @SuppressWarnings({
    "java:S1940" /* This violation will be rewritten. */,
    "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
  })
  static final class EqualToWithBoolean {
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
    boolean before(Object a, Object b) {
      return !(a != b);
    }

    @AfterTemplate
    boolean after(boolean a, boolean b) {
      return a == b;
    }
  }

  /** Prefer negated lambda expressions over more contrived alternatives. */
  abstract static class Not<T> {
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

  /** Prefer {@link Object#equals(Object)} over more contrived alternatives. */
  static final class TEqualsWithObject<T, S> {
    @BeforeTemplate
    boolean before(T value, S obj) {
      return Refaster.anyOf(
          Optional.of(value).equals(Optional.of(obj)),
          Optional.of(value).equals(Optional.ofNullable(obj)),
          Optional.ofNullable(obj).equals(Optional.of(value)));
    }

    @AfterTemplate
    boolean after(T value, S obj) {
      return value.equals(obj);
    }
  }

  /** Prefer {@link Objects#equals(Object, Object)} over more contrived alternatives. */
  static final class ObjectsEquals<T, S> {
    @BeforeTemplate
    boolean before(T a, S b) {
      return Optional.ofNullable(a).equals(Optional.ofNullable(b));
    }

    @AfterTemplate
    boolean after(T a, S b) {
      return Objects.equals(a, b);
    }
  }
}
