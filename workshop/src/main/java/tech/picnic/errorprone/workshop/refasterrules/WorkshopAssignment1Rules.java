package tech.picnic.errorprone.workshop.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rules for the first assignment of the workshop. */
final class WorkshopAssignment1Rules {
  private WorkshopAssignment1Rules() {}

  /** Prefer {@link String#String(char[])} over {@link String#copyValueOf(char[])}. */
  static final class NewStringCharArray {
    @BeforeTemplate
    String before(char[] chars) {
      return String.copyValueOf(chars);
    }

    @AfterTemplate
    String after(char[] chars) {
      return new String(chars);
    }
  }
}
