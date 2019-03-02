package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Don't call {@link ImmutableList#asList()}; it is a no-op. */
// XXX: Maybe file a Guava PR to mark this method deprecated?
final class ImmutableListAsList<T> {
  @BeforeTemplate
  ImmutableList<T> before(ImmutableList<T> list) {
    return list.asList();
  }

  @AfterTemplate
  ImmutableList<T> after(ImmutableList<T> list) {
    return list;
  }
}
