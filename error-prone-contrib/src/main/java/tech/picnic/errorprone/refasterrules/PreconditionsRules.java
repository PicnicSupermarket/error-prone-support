package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;

/** Refaster templates related to expressions dealing with {@link Preconditions}. */
final class PreconditionsRules {
  private PreconditionsRules() {}

  /**
   * Prefer {@link Preconditions#checkArgument(boolean)} over the more verbose conditional {@code
   * throw new IllegalArgumentException()}.
   */
  static final class CheckArgumentEmpty {
    @BeforeTemplate
    void before(boolean condition) {
      if (condition) {
        throw new IllegalArgumentException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      checkArgument(!condition);
    }
  }

  /**
   * Prefer {@link Preconditions#checkArgument(boolean, Object)} over the more verbose conditional
   * {@code throw new IllegalArgumentException(String)}.
   */
  static final class CheckArgumentMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      if (condition) {
        throw new IllegalArgumentException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      checkArgument(!condition, message);
    }
  }

  /**
   * Prefer {@link Preconditions#checkState(boolean)} over the more verbose conditional {@code throw
   * new IllegalStateException()}.
   */
  static final class CheckStateEmpty {
    @BeforeTemplate
    void before(boolean condition) {
      if (condition) {
        throw new IllegalStateException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      checkState(!condition);
    }
  }

  /**
   * Prefer {@link Preconditions#checkState(boolean, Object)} over the more verbose conditional
   * {@code throw new IllegalStateException(String)}.
   */
  static final class CheckStateMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      if (condition) {
        throw new IllegalStateException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      checkState(!condition, message);
    }
  }
}
