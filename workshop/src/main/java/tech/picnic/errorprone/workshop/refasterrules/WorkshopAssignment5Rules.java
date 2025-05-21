package tech.picnic.errorprone.workshop.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import java.util.function.Supplier;

/** Refaster rules for the fifth assignment of the workshop. */
final class WorkshopAssignment5Rules {
  private WorkshopAssignment5Rules() {}

  /**
   * Prefer {@link Optional#orElse(Object)} over {@link Optional#orElseGet(Supplier)} if the
   * fallback value does not require non-trivial computation.
   */
  // XXX: Implement the Refaster rule to get the test green.
  // Tip: use the `@NotMatches` or `@Matches` annotation with the
  // `tech.picnic.errorprone.refaster.matchers.RequiresComputation.RequiresComputation` Matcher.
  // Tip: check the associated input and output files in test resources.
  static final class OptionalOrElseIfItDoesntRequireComputation<T> {
    @BeforeTemplate
    T before(Optional<T> optional, T value) {
      return optional.orElseGet(() -> value);
    }

    @AfterTemplate
    T after(Optional<T> optional, T value) {
      return optional.orElse(value);
    }
  }
}
