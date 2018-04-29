package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;

/** Prefer {@link Optional#isEmpty()} over the more verbose alternative. */
final class OptionalIsEmpty<T> {
  @BeforeTemplate
  boolean before(Optional<T> optional) {
    return !optional.isPresent();
  }

  @AfterTemplate
  boolean after(Optional<T> optional) {
    return optional.isEmpty();
  }
}
