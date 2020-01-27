package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;

/** Refaster templates related to expressions dealing with (possibly) null values. */
final class MethodTemplates {
  private MethodTemplates() {}

  /** Prefer {@link Objects#requireNonNullElse(Object, Object)} over the Guava alternative. */
  static final class DirectlyReturnInsteadOfFirstAssignVariable<T> {
    @BeforeTemplate
    T before(T element) {
      T var = element;
      return var;
    }

    @AfterTemplate
    T after(T element) {
      return element;
    }
  }
}
