package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import com.google.common.base.MoreObjects;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with (possibly) null values. */
@OnlineDocumentation
final class NullRules {
  private NullRules() {}

  /** Prefer the {@code ==} operator over {@link Objects#isNull(Object)}. */
  static final class IsNull {
    @BeforeTemplate
    boolean before(@Nullable Object object) {
      return Objects.isNull(object);
    }

    @AfterTemplate
    boolean after(@Nullable Object object) {
      return object == null;
    }
  }

  /** Prefer the {@code !=} operator over {@link Objects#nonNull(Object)}. */
  static final class IsNotNull {
    @BeforeTemplate
    boolean before(@Nullable Object object) {
      return Objects.nonNull(object);
    }

    @AfterTemplate
    boolean after(@Nullable Object object) {
      return object != null;
    }
  }

  /**
   * Prefer {@link Objects#requireNonNullElse(Object, Object)} over the Guava and Optional
   * alternatives.
   */
  // XXX: This rule is not valid in case `second` is `@Nullable`: in that case the Guava and
  // Optional variants
  // will return `null`, while the Objects.requireNonNullElse variant will throw an NPE.
  static final class RequireNonNullElse<T> {
    @BeforeTemplate
    T before(T first, T second) {
      return MoreObjects.firstNonNull(first, second);
    }

    @BeforeTemplate
    T beforeOptional(T first, T second) {
      return Optional.ofNullable(first).orElse(second);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T first, T second) {
      return requireNonNullElse(first, second);
    }
  }

  /**
   * Prefer {@link Objects#requireNonNullElseGet(Object, Supplier)} over {@link Optional}{@code
   * .ofNullable(Object).orElseGet(Supplier)}
   */
  // XXX: This rule is not valid in case `supplier` may return `null`: Optional will return `null`,
  // while the `requireNonNullElseGet` replacement will throw an NPE.
  static final class RequireNonNullElseGet<T, S extends Supplier<T>> {
    @BeforeTemplate
    T before(T object, S defaultSupplier) {
      return Optional.ofNullable(object).orElseGet(defaultSupplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T object, S defaultSupplier) {
      return requireNonNullElseGet(object, defaultSupplier);
    }
  }

  /** Prefer {@link Objects#isNull(Object)} over the equivalent lambda function. */
  static final class IsNullFunction<T> {
    @BeforeTemplate
    Predicate<T> before() {
      return o -> o == null;
    }

    @AfterTemplate
    Predicate<T> after() {
      return Objects::isNull;
    }
  }

  /** Prefer {@link Objects#nonNull(Object)} over the equivalent lambda function. */
  static final class NonNullFunction<T> {
    @BeforeTemplate
    Predicate<T> before() {
      return o -> o != null;
    }

    @AfterTemplate
    Predicate<T> after() {
      return Objects::nonNull;
    }
  }
}
