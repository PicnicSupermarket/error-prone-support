package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NoAutoboxing;
import java.util.Objects;
import java.util.function.Predicate;

/** Refaster templates related to expressions dealing with (in)equalities. */
final class Equality {
  private Equality() {}

  /** Avoid boxing when comparing primitive values. */
  static final class PrimitiveEquals {
    @NoAutoboxing
    @BeforeTemplate
    boolean before(long a, long b) {
      return Objects.equals(a, b);
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(long a, long b) {
      return a == b;
    }
  }

  /**
   * Enums can be compared by reference. It is safe to do so even in the face of refactorings,
   * because if the type is ever converted to a non-enum, then Error-Prone will complain about any
   * remaining reference-based equality checks.
   */
  // XXX: This Refaster rule is defined in terms of an inequality because of
  // https://github.com/google/error-prone/issues/559
  static final class EnumEquals<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("boxing")
    boolean before(T a, T b) {
      return !Refaster.anyOf(a.equals(b), Objects.equals(a, b));
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(T a, T b) {
      return a != b;
    }
  }

  /** Prefer {@link Object#equals(Object)} over the equivalent lambda function. */
  // XXX: As it stands, this rule is a special case of what `MethodReferenceUsageCheck` tries to
  // achieve. If/when `MethodReferenceUsageCheck` becomes production ready, we should simply drop
  // this check.
  // XXX: Alternatively, rule should be replaced with a plugin which also identifies cases where the
  // arguments are swapped but simplification is possible anyway, by virtue of `v` being non-null.
  static final class EqualsPredicate<T> {
    @BeforeTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> before(T v) {
      return e -> v.equals(e);
    }

    @AfterTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> after(T v) {
      return v::equals;
    }
  }

  /**
   * Don't use the ternary operator to compare two booleans.
   *
   * @see UnequalBooleans
   */
  static final class EqualBooleans {
    @BeforeTemplate
    boolean before(boolean b1, boolean b2) {
      return b1 ? b2 : !b2;
    }

    @AfterTemplate
    boolean after(boolean b1, boolean b2) {
      return b1 == b2;
    }
  }

  /**
   * Don't use the ternary operator to compare two booleans.
   *
   * @see EqualBooleans
   */
  static final class UnequalBooleans {
    @BeforeTemplate
    boolean before(boolean b1, boolean b2) {
      return b1 ? !b2 : b2;
    }

    @AfterTemplate
    boolean after(boolean b1, boolean b2) {
      return b1 != b2;
    }
  }
}
