package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Class}es. */
@OnlineDocumentation
final class ClassRules {
  private ClassRules() {}

  /** Prefer {@link Class#isInstance(Object)} over more contrived or more fragile alternatives. */
  static final class ClassIsInstance<T, S> {
    @BeforeTemplate
    boolean before(Class<T> clazz, S object) {
      return clazz.isAssignableFrom(object.getClass());
    }

    @AfterTemplate
    boolean after(Class<T> clazz, S object) {
      return clazz.isInstance(object);
    }
  }

  /** Prefer using the {@code instanceof} keyword over less idiomatic alternatives. */
  static final class Instanceof<T, S> {
    @BeforeTemplate
    boolean before(S object) {
      return Refaster.<T>clazz().isInstance(object);
    }

    @AfterTemplate
    boolean after(S object) {
      return Refaster.<T>isInstance(object);
    }
  }

  /** Prefer {@link Class#isInstance(Object)} method references over more verbose alternatives. */
  // XXX: Once the `ClassReferenceIsInstancePredicate` rule is dropped, rename this rule to just
  // `ClassIsInstancePredicate`.
  static final class ClassLiteralIsInstancePredicate<T, S> {
    @BeforeTemplate
    Predicate<S> before() {
      return o -> Refaster.<T>isInstance(o);
    }

    @AfterTemplate
    Predicate<S> after() {
      return Refaster.<T>clazz()::isInstance;
    }
  }

  /** Prefer {@link Class#isInstance(Object)} method references over more verbose alternatives. */
  // XXX: Drop this rule once the `MethodReferenceUsage` rule is enabled by default.
  static final class ClassReferenceIsInstancePredicate<T, S> {
    @BeforeTemplate
    Predicate<S> before(Class<T> clazz) {
      return o -> clazz.isInstance(o);
    }

    @AfterTemplate
    Predicate<S> after(Class<T> clazz) {
      return clazz::isInstance;
    }
  }

  /** Prefer {@link Class#cast(Object)} method references over more verbose alternatives. */
  // XXX: Drop this rule once the `MethodReferenceUsage` rule is enabled by default.
  static final class ClassReferenceCast<T, S, U extends S> {
    @BeforeTemplate
    Function<T, S> before(Class<U> clazz) {
      return o -> clazz.cast(o);
    }

    @AfterTemplate
    Function<T, S> after(Class<U> clazz) {
      return clazz::cast;
    }
  }
}
