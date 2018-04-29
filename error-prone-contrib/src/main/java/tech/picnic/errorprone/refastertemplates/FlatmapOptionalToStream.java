package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Flatten a stream of {@link Optional}s using {@link Optional#stream()} instead of the more verbose
 * alternative.
 */
final class FlatmapOptionalToStream<T> {
  @BeforeTemplate
  Stream<T> before(Stream<Optional<T>> stream) {
    return Refaster.anyOf(
        stream.filter(Optional::isPresent).map(Optional::get), stream.flatMap(Streams::stream));
  }

  @AfterTemplate
  Stream<T> after(Stream<Optional<T>> stream) {
    return stream.flatMap(Optional::stream);
  }
}
