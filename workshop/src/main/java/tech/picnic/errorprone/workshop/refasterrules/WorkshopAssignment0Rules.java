package tech.picnic.errorprone.workshop.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rule used as example for the assignments of the workshop. */
final class WorkshopAssignment0Rules {
  private WorkshopAssignment0Rules() {}

  /** Prefer {@link String#isEmpty()} over alternatives that consult the string's length. */
  static final class ExampleStringIsEmpty {
    @BeforeTemplate
    boolean before(String s) {
      return s.length() == 0;
    }

    @AfterTemplate
    boolean after(String s) {
      return s.isEmpty();
    }
  }
}
