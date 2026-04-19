package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.function.Predicate.not;

import com.google.common.base.MoreObjects;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
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

  /** Prefer {@code == null} over less idiomatic alternatives. */
  static final class EqualToNull {
    @BeforeTemplate
    boolean before(@Nullable Object obj) {
      return Refaster.anyOf(null == obj, Objects.isNull(obj));
    }

    @AfterTemplate
    boolean after(@Nullable Object obj) {
      return obj == null;
    }
  }

  /** Prefer {@code != null} over less idiomatic alternatives. */
  static final class NotEqualToNull {
    @BeforeTemplate
    boolean before(@Nullable Object obj) {
      return Refaster.anyOf(null != obj, Objects.nonNull(obj));
    }

    @AfterTemplate
    boolean after(@Nullable Object obj) {
      return obj != null;
    }
  }

  /**
   * Prefer {@link Objects#requireNonNullElse(Object, Object)} over non-JDK or more contrived
   * alternatives.
   */
  // XXX: This rule is not valid in case `second` is `@Nullable`: in that case the Guava and
  // `Optional` variants will return `null`, where the `requireNonNullElse` alternative will throw
  // an NPE.
  static final class RequireNonNullElse<T> {
    @BeforeTemplate
    T before(T obj, T defaultObj) {
      return Refaster.anyOf(
          MoreObjects.firstNonNull(obj, defaultObj), Optional.ofNullable(obj).orElse(defaultObj));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T obj, T defaultObj) {
      return requireNonNullElse(obj, defaultObj);
    }
  }

  /**
   * Prefer {@link Objects#requireNonNullElseGet(Object, Supplier)} over more contrived
   * alternatives.
   */
  // XXX: This rule is not valid in case `supplier` yields `@Nullable` values: in that case the
  // `Optional` variant will return `null`, where the `requireNonNullElseGet` alternative will throw
  // an NPE.
  static final class RequireNonNullElseGet<T, S extends T> {
    @BeforeTemplate
    T before(T obj, Supplier<S> supplier) {
      return Optional.ofNullable(obj).orElseGet(supplier);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T obj, Supplier<S> supplier) {
      return requireNonNullElseGet(obj, supplier);
    }
  }

  /** Prefer {@link Objects#isNull(Object)} over less idiomatic or more contrived alternatives. */
  static final class ObjectsIsNull<T> {
    @BeforeTemplate
    Predicate<T> before() {
      return Refaster.anyOf(o -> o == null, not(Objects::nonNull));
    }

    @AfterTemplate
    Predicate<T> after() {
      return Objects::isNull;
    }
  }

  /** Prefer {@link Objects#nonNull(Object)} over less idiomatic or more contrived alternatives. */
  static final class ObjectsNonNull<T> {
    @BeforeTemplate
    Predicate<T> before() {
      return Refaster.anyOf(o -> o != null, not(Objects::isNull));
    }

    @AfterTemplate
    Predicate<T> after() {
      return Objects::nonNull;
    }
  }
}
