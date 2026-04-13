package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Preconditions}. */
@OnlineDocumentation
final class PreconditionsRules {
  private PreconditionsRules() {}

  /** Prefer {@link Preconditions#checkArgument(boolean)} over more verbose alternatives. */
  static final class CheckArgumentNot {
    @BeforeTemplate
    void before(boolean b) {
      if (b) {
        throw new IllegalArgumentException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean b) {
      checkArgument(!b);
    }
  }

  /** Prefer {@link Preconditions#checkArgument(boolean, Object)} over more verbose alternatives. */
  static final class CheckArgumentNotWithString {
    @BeforeTemplate
    void before(boolean b, String errorMessage) {
      if (b) {
        throw new IllegalArgumentException(errorMessage);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean b, String errorMessage) {
      checkArgument(!b, errorMessage);
    }
  }

  /**
   * Prefer {@link Preconditions#checkElementIndex(int, int, String)} over less explicit or more
   * verbose alternatives.
   *
   * <p>Note that the two-argument {@link Preconditions#checkElementIndex(int, int)} is better
   * replaced with {@link java.util.Objects#checkIndex(int, int)}.
   */
  static final class CheckElementIndex {
    @BeforeTemplate
    void before(int index, int size, String desc) {
      if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException(desc);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size, String desc) {
      checkElementIndex(index, size, desc);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object)} over non-JDK alternatives. */
  static final class RequireNonNullExpression<T> {
    @BeforeTemplate
    T before(T obj) {
      return checkNotNull(obj);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T obj) {
      return requireNonNull(obj);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object)} over more verbose alternatives. */
  static final class RequireNonNullBlock<T extends @Nullable Object> {
    // XXX: Drop the `java:S2583` violation suppression once SonarCloud better supports JSpecify
    // annotations.
    @BeforeTemplate
    @SuppressWarnings({
      "java:S1695" /* This violation will be rewritten. */,
      "java:S2583" /* SonarCloud incorrectly believes that `object` is not `@Nullable`. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    void before(T obj) {
      if (obj == null) {
        throw new NullPointerException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T obj) {
      requireNonNull(obj);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object, String)} over non-JDK alternatives. */
  static final class RequireNonNullWithStringExpression<T> {
    @BeforeTemplate
    T before(T obj, String message) {
      return checkNotNull(obj, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T obj, String message) {
      return requireNonNull(obj, message);
    }
  }

  /** Prefer {@link Objects#requireNonNull(Object, String)} over more verbose alternatives. */
  static final class RequireNonNullWithStringBlock<T extends @Nullable Object> {
    // XXX: Drop the `java:S2583` violation suppression once SonarCloud better supports JSpecify
    // annotations.
    @BeforeTemplate
    @SuppressWarnings({
      "java:S1695" /* This violation will be rewritten. */,
      "java:S2583" /* SonarCloud incorrectly believes that `object` is not `@Nullable`. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    void before(T obj, String message) {
      if (obj == null) {
        throw new NullPointerException(message);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T obj, String message) {
      requireNonNull(obj, message);
    }
  }

  /**
   * Prefer {@link Preconditions#checkPositionIndex(int, int)} over less explicit or more verbose
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
   * Prefer {@link Preconditions#checkPositionIndex(int, int, String)} over less explicit or more
   * verbose alternatives.
   */
  static final class CheckPositionIndexWithString {
    @BeforeTemplate
    void before(int index, int size, String desc) {
      if (index < 0 || index > size) {
        throw new IndexOutOfBoundsException(desc);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size, String desc) {
      checkPositionIndex(index, size, desc);
    }
  }

  /** Prefer {@link Preconditions#checkState(boolean)} over more verbose alternatives. */
  static final class CheckStateNot {
    @BeforeTemplate
    void before(boolean b) {
      if (b) {
        throw new IllegalStateException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean b) {
      checkState(!b);
    }
  }

  /** Prefer {@link Preconditions#checkState(boolean, Object)} over more verbose alternatives. */
  static final class CheckStateNotWithString {
    @BeforeTemplate
    void before(boolean b, String errorMessage) {
      if (b) {
        throw new IllegalStateException(errorMessage);
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean b, String errorMessage) {
      checkState(!b, errorMessage);
    }
  }
}
