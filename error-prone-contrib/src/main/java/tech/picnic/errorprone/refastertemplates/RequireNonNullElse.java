package tech.picnic.errorprone.refastertemplates;

import com.google.common.base.MoreObjects;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;

/** Prefer {@link Objects#requireNonNullElse(Object, Object)} over the Guava alternative. */
final class RequireNonNullElse<T> {
  @BeforeTemplate
  T before(T first, T second) {
    return MoreObjects.firstNonNull(first, second);
  }

  @AfterTemplate
  T after(T first, T second) {
    return Objects.requireNonNullElse(first, second);
  }
}
