package tech.picnic.errorprone.refastertemplates.collections;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/** Prefer {@link ImmutableSet#toImmutableSet()} over the more verbose alternative. */
final class StreamToImmutableSet<T> {
  @BeforeTemplate
  ImmutableSet<T> before(Stream<T> stream) {
    return ImmutableSet.copyOf(stream.iterator());
  }

  @AfterTemplate
  @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  ImmutableSet<T> after(Stream<T> stream) {
    return stream.collect(toImmutableSet());
  }
}
