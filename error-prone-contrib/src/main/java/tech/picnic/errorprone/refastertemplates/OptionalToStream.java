package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import java.util.stream.Stream;

/** Prefer {@link Optional#stream()} over the Guava alternative. */
final class OptionalToStream<T> {
  @BeforeTemplate
  Stream<T> before(Optional<T> optional) {
    return Streams.stream(optional);
  }

  @AfterTemplate
  Stream<T> after(Optional<T> optional) {
    return optional.stream();
  }
}
