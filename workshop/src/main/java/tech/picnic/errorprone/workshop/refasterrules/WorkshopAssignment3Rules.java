package tech.picnic.errorprone.workshop.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;

/** Refaster rules for the third assignment of the workshop. */
final class WorkshopAssignment3Rules {
  private WorkshopAssignment3Rules() {}

  /** Prefer {@link Preconditions#checkArgument(boolean)} over if statements. */
  static final class CheckArgumentWithoutMessage {
    @BeforeTemplate
    void before(boolean expression) {
      if (expression) {
        throw new IllegalArgumentException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean expression) {
      checkArgument(!expression);
    }
  }

  /** Prefer {@link Preconditions#checkArgument(boolean, Object)} over if statements. */
  static final class CheckArgumentWithMessage {
    @BeforeTemplate
    void before(boolean expression, String message) {
      if (expression) {
        throw new IllegalArgumentException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean expression, String message) {
      checkArgument(!expression, message);
    }
  }
}
