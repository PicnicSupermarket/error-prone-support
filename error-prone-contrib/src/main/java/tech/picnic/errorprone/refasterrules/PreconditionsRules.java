package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;

/** Refaster templates related to statements dealing with {@link Preconditions}. */
final class PreconditionsRules {
  private PreconditionsRules() {}

  /** Prefer {@link Preconditions#checkArgument(boolean)} over more verbose alternatives. */
  static final class CheckArgument {
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

  /** Prefer {@link Preconditions#checkArgument(boolean, Object)} over more verbose alternatives. */
  static final class CheckArgumentWithMessage {
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

  // XXX: Also suggest `checkElementIndex` usage.

  /** Prefer {@link Preconditions#checkNotNull(Object)} over more verbose alternatives. */
  static final class CheckNotNull<T> {
    @BeforeTemplate
    void before(T object) {
      if (object == null) {
        throw new NullPointerException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T object) {
      checkNotNull(object);
    }
  }

  /** Prefer {@link Preconditions#checkNotNull(Object, Object)} over more verbose alternatives. */
  static final class CheckNotNullWithMessage<T> {
    @BeforeTemplate
    void before(T object, String message) {
      if (object == null) {
        throw new NullPointerException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T object, String message) {
      checkNotNull(object, message);
    }
  }

  // XXX: Also suggest `checkPositionIndex` usage.

  /** Prefer {@link Preconditions#checkState(boolean)} over more verbose alternatives. */
  static final class CheckState {
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

  /** Prefer {@link Preconditions#checkState(boolean, Object)} over more verbose alternatives. */
  static final class CheckStateWithMessage {
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
