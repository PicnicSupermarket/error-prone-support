package tech.picnic.errorprone.refastertemplates.collections;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import com.google.common.collect.ImmutableMultiset;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/** Prefer {@link ImmutableMultiset#toImmutableMultiset()} over the more verbose alternative. */
final class StreamToImmutableMultiset<T> {
  @BeforeTemplate
  ImmutableMultiset<T> before(Stream<T> stream) {
    return ImmutableMultiset.copyOf(stream.iterator());
  }

  @AfterTemplate
  @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  ImmutableMultiset<T> after(Stream<T> stream) {
    return stream.collect(toImmutableMultiset());
  }
}
