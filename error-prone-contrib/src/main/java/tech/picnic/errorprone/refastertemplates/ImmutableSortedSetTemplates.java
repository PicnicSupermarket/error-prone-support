package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Comparator;

/** Refaster templates related to expressions dealing with {@link ImmutableSortedSet}s. */
final class ImmutableSortedSetTemplates {
  private ImmutableSortedSetTemplates() {}

  /** Prefer {@link ImmutableSortedSet#orderedBy(Comparator)} over the associated constructor. */
  static final class ImmutableSortedSetBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet.Builder<T> before(Comparator<T> cmp) {
      return new ImmutableSortedSet.Builder<>(cmp);
    }

    @AfterTemplate
    ImmutableSortedSet.Builder<T> after(Comparator<T> cmp) {
      return ImmutableSortedSet.orderedBy(cmp);
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#naturalOrder()} over the alternative that requires explicitly
   * providing the {@link Comparator}.
   */
  static final class ImmutableSortedSetNaturalOrderBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet.Builder<T> before() {
      return ImmutableSortedSet.orderedBy(Comparator.<T>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedSet.Builder<T> after(Comparator<T> cmp) {
      return ImmutableSortedSet.naturalOrder();
    }
  }
}
