package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.function.Predicate;

/** Prefer {@link Object#equals(Object)} over the equivalent lambda function. */
// XXX: This rule should be replaced with a nullness-aware plugin which identifies all such
// opportunities.
final class EqualsPredicate<T> {
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
