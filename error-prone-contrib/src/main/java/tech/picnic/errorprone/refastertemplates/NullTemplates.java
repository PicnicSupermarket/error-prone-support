package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.requireNonNullElse;

import com.google.common.base.MoreObjects;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Objects;
import java.util.function.Predicate;

/** Refaster templates related to expressions dealing with (possibly) null values. */
final class NullTemplates {
  private NullTemplates() {}

  /** Prefer {@link Objects#requireNonNullElse(Object, Object)} over the Guava alternative. */
  // XXX: This rule is not valid in case `second` is `@Nullable`: in that case the Guava variant
  // will return `null`, while the JDK variant will throw an NPE.
  static final class RequireNonNullElse<T> {
    @BeforeTemplate
    T before(T first, T second) {
      return MoreObjects.firstNonNull(first, second);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T first, T second) {
      return requireNonNullElse(first, second);
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
