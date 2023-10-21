package tech.picnic.errorprone.workshop.refasterrules;

/** Refaster rule used as example for the assignments of the workshop. */
final class WorkshopAssignment0Rules {
  private WorkshopAssignment0Rules() {}

  /** Prefer {@link String#isEmpty()} over alternatives that consult the string's length. */
  static final class ExampleStringIsEmpty {}
}
