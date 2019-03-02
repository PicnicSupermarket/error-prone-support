package tech.picnic.errorprone.refastertemplates;

import com.google.common.base.MoreObjects;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;
import java.util.function.Predicate;

/** Refaster templates related to expressions dealing with (possibly) null values. */
final class Nulls {
  private Nulls() {}

  /** Prefer {@link Objects#requireNonNullElse(Object, Object)} over the Guava alternative. */
  static final class RequireNonNullElse<T> {
    @BeforeTemplate
    T before(T first, T second) {
      return MoreObjects.firstNonNull(first, second);
    }

    @AfterTemplate
    T after(T first, T second) {
      return Objects.requireNonNullElse(first, second);
    }
  }

  /** Prefer {@link Objects#isNull(Object)} over the equivalent lambda function. */
  static final class IsNullFunction<T> {
    @BeforeTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> before() {
      return o -> o == null;
    }

    @AfterTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> after() {
      return Objects::isNull;
    }
  }

  /** Prefer {@link Objects#nonNull(Object)} over the equivalent lambda function. */
  static final class NonNullFunction<T> {
    @BeforeTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> before() {
      return o -> o != null;
    }

    @AfterTemplate
    @SuppressWarnings("NoFunctionalReturnType")
    Predicate<T> after() {
      return Objects::nonNull;
    }
  }
}
