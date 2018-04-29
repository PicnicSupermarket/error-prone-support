package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;

/** Prefer {@link Optional#isPresent()} over the inverted alternative. */
final class OptionalIsPresent<T> {
  @BeforeTemplate
  boolean before(Optional<T> optional) {
    return !optional.isEmpty();
  }

  @AfterTemplate
  boolean after(Optional<T> optional) {
    return optional.isPresent();
  }
}
