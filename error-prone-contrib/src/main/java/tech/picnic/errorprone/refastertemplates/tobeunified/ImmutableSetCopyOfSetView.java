package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets.SetView;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Prefer {@link SetView#immutableCopy()} over the more verbose alternative. */
final class ImmutableSetCopyOfSetView<T> {
  @BeforeTemplate
  ImmutableSet<T> before(SetView<T> set) {
    return ImmutableSet.copyOf(set);
  }

  @AfterTemplate
  ImmutableSet<T> after(SetView<T> set) {
    return set.immutableCopy();
  }
}
