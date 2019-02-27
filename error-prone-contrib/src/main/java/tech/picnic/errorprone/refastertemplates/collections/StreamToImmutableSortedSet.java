package tech.picnic.errorprone.refastertemplates.collections;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/**
 * Prefer {@link ImmutableSortedSet#toImmutableSortedSet(java.util.Comparator)} over the less
 * idiomatic alternative.
 */
final class StreamToImmutableSortedSet<T extends Comparable<? super T>> {
  @BeforeTemplate
  ImmutableSortedSet<T> before(Stream<T> stream) {
    return ImmutableSortedSet.copyOf(stream.iterator());
  }

  @AfterTemplate
  @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  ImmutableSortedSet<T> after(Stream<T> stream) {
    return stream.collect(toImmutableSortedSet(naturalOrder()));
  }
}
