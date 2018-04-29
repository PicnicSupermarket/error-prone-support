package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;
import java.util.function.Predicate;

/** Prefer {@link Objects#isNull(Object)} over the equivalent lambda function. */
final class IsNullFunction<T> {
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
