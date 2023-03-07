package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Objects;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster templates related to statements dealing with {@link Preconditions}. */
@OnlineDocumentation
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

  /**
   * Prefer {@link Preconditions#checkElementIndex(int, int, String)} over less descriptive or more
   * verbose alternatives.
   *
   * <p>Note that the two-argument {@link Preconditions#checkElementIndex(int, int)} is better
   * replaced with {@link java.util.Objects#checkIndex(int, int)}.
   */
  static final class CheckElementIndexWithMessage {
    @BeforeTemplate
    void before(int index, int size, String message) {
      if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size, String message) {
      checkElementIndex(index, size, message);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object)} over more verbose alternatives. */
  static final class RequireNonNull<T> {
    @BeforeTemplate
    void before(T object) {
      if (Refaster.anyOf(object == null, null == object)) {
        throw new NullPointerException();
      }
    }

    @BeforeTemplate
    void before2(T object) {
      checkNotNull(object);
    }

    @BeforeTemplate
    void before3(T object) {
      checkArgument(Refaster.anyOf(object != null, null != object));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T object) {
      requireNonNull(object);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object, String)} over more verbose alternatives. */
  static final class RequireNonNullWithMessage<T> {
    @BeforeTemplate
    void before(T object, String message) {
      if (Refaster.anyOf(object == null, null == object)) {
        throw new NullPointerException(message);
      }
    }

    @BeforeTemplate
    void before2(T object, String message) {
      checkNotNull(object, message);
    }

    @BeforeTemplate
    void before3(T object, String message) {
      checkArgument(Refaster.anyOf(object != null, null != object), message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T object, String message) {
      requireNonNull(object, message);
    }
  }

  /**
   * Prefer {@link Preconditions#checkPositionIndex(int, int)} over less descriptive or more verbose
   * alternatives.
   */
  static final class CheckPositionIndex {
    @BeforeTemplate
    void before(int index, int size) {
      if (index < 0 || index > size) {
        throw new IndexOutOfBoundsException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size) {
      checkPositionIndex(index, size);
    }
  }

  /**
   * Prefer {@link Preconditions#checkPositionIndex(int, int, String)} over less descriptive or more
   * verbose alternatives.
   */
  static final class CheckPositionIndexWithMessage {
    @BeforeTemplate
    void before(int index, int size, String message) {
      if (index < 0 || index > size) {
        throw new IndexOutOfBoundsException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size, String message) {
      checkPositionIndex(index, size, message);
    }
  }

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
