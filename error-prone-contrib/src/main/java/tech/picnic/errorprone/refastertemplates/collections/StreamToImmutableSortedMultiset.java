package tech.picnic.errorprone.refastertemplates.collections;

import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSortedMultiset;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/**
 * Prefer {@link ImmutableSortedMultiset#toImmutableSortedMultiset(java.util.Comparator)} over the
 * less idiomatic alternative.
 */
final class StreamToImmutableSortedMultiset<T extends Comparable<? super T>> {
  @BeforeTemplate
  ImmutableSortedMultiset<T> before(Stream<T> stream) {
    return ImmutableSortedMultiset.copyOf(stream.iterator());
  }

  @AfterTemplate
  @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  ImmutableSortedMultiset<T> after(Stream<T> stream) {
    return stream.collect(toImmutableSortedMultiset(naturalOrder()));
  }
}
