package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets.SetView;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster templates related to expressions dealing with {@link ImmutableSet}s. */
final class ImmutableSetTemplates {
  private ImmutableSetTemplates() {}

  static final class ImmutableSetBuilder<T> {
    @BeforeTemplate
    ImmutableSet.Builder<T> before() {
      return new ImmutableSet.Builder<>();
    }

    @AfterTemplate
    ImmutableSet.Builder<T> after() {
      return ImmutableSet.builder();
    }
  }

  /** Prefer {@link SetView#immutableCopy()} over the more verbose alternative. */
  static final class ImmutableSetCopyOfSetView<T> {
    @BeforeTemplate
    ImmutableSet<T> before(SetView<T> set) {
      return ImmutableSet.copyOf(set);
    }

    @AfterTemplate
    ImmutableSet<T> after(SetView<T> set) {
      return set.immutableCopy();
    }
  }
}
