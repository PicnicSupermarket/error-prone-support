package tech.picnic.errorprone.refastertemplates.collections;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/** Prefer {@link ImmutableList#toImmutableList()} over the more verbose alternative. */
final class StreamToImmutableList<T> {
  @BeforeTemplate
  ImmutableList<T> before(Stream<T> stream) {
    return ImmutableList.copyOf(stream.iterator());
  }

  @AfterTemplate
  @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  ImmutableList<T> after(Stream<T> stream) {
    return stream.collect(toImmutableList());
  }
}
