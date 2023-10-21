package tech.picnic.errorprone.workshop.refasterrules;

import com.google.common.base.Preconditions;

/** Refaster rules for the third assignment of the workshop. */
@SuppressWarnings("UnusedTypeParameter" /* Ignore this for demo purposes. */)
final class WorkshopAssignment3Rules {
  private WorkshopAssignment3Rules() {}

  // XXX: Tip: check the input and output files to see the *expected* refactoring.

  /** Prefer {@link Preconditions#checkArgument(boolean)} over if statements. */
  static final class CheckArgumentWithoutMessage {
    // XXX: Implement the Refaster rule to get the test green.
  }

  /** Prefer {@link Preconditions#checkArgument(boolean, Object)} over if statements. */
  static final class CheckArgumentWithMessage {
    // XXX: Implement the Refaster rule to get the test green.
  }
}
