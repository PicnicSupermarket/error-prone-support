package tech.picnic.errorprone.workshop.refasterrules;

import com.google.common.collect.ImmutableList;

/** Refaster rules for the second assignment of the workshop. */
@SuppressWarnings("UnusedTypeParameter" /* Ignore this for demo purposes. */)
final class WorkshopAssignment2Rules {
  private WorkshopAssignment2Rules() {}

  /**
   * Prefer {@link ImmutableList#of(Object)} over alternatives that don't communicate the
   * immutability of the resulting list at the type level.
   */
  static final class ImmutableListOfOne<T> {
    // XXX: Implement this Refaster rule.
  }
}
