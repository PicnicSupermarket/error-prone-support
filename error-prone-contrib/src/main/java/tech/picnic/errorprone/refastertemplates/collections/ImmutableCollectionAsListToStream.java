package tech.picnic.errorprone.refastertemplates.collections;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.stream.Stream;

/**
 * Don't call {@link ImmutableList#asList()} just to convert the result to a stream; this has no
 * benefit.
 */
final class ImmutableCollectionAsListToStream<T> {
  @BeforeTemplate
  Stream<T> before(ImmutableCollection<T> collection) {
    return collection.asList().stream();
  }

  @AfterTemplate
  Stream<T> after(ImmutableCollection<T> collection) {
    return collection.stream();
  }
}
