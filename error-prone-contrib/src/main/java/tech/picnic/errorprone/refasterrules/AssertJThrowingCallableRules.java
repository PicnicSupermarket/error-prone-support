package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.io.IOException;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableAssertAlternative;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over expressions that may throw a {@link Throwable}
 * subtype.
 *
 * <p>For reasons of consistency we prefer {@link
 * org.assertj.core.api.Assertions#assertThatThrownBy} over static methods for specific exception
 * types. Note that only the most common assertion expressions are rewritten here; covering all
 * cases would require the implementation of an Error Prone check instead.
 */
@OnlineDocumentation
final class AssertJThrowingCallableRules {
  private AssertJThrowingCallableRules() {}

  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      Refaster.anyOf(
          assertThatThrownBy(throwingCallable).asInstanceOf(throwable(exceptionType)),
          assertThatThrownBy(throwingCallable).asInstanceOf(type(exceptionType)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType);
    }
  }

  static final class AssertThatThrownByIllegalArgumentException {
    @BeforeTemplate
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable) {
      return assertThatIllegalArgumentException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message, parameters);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith(message);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(message);
    }
  }

  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageNotContainingAny {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable, @Repeated CharSequence values) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageNotContainingAny(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable, @Repeated CharSequence values) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageNotContainingAny(values);
    }
  }

  static final class AssertThatThrownByIllegalStateException {
    @BeforeTemplate
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable) {
      return assertThatIllegalStateException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IllegalStateException.class);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message, parameters);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith(message);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(message);
    }
  }

  static final class AssertThatThrownByIllegalStateExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageNotContaining(message);
    }
  }

  static final class AssertThatThrownByNullPointerException {
    @BeforeTemplate
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable) {
      return assertThatNullPointerException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message, parameters);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageStartingWith(message);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining(message);
    }
  }

  static final class AssertThatThrownByNullPointerExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageNotContaining(message);
    }
  }

  static final class AssertThatThrownByIOException {
    @BeforeTemplate
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable) {
      return assertThatIOException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IOException.class);
    }
  }

  static final class AssertThatThrownByIOExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIOExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByIOExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessage(message, parameters);
    }
  }

  static final class AssertThatThrownByIOExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageStartingWith(message);
    }
  }

  static final class AssertThatThrownByIOExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageContaining(message);
    }
  }

  static final class AssertThatThrownByIOExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageNotContaining(message);
    }
  }

  static final class AssertThatThrownByAsInstanceOfThrowable<T extends Throwable> {
    @BeforeTemplate
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType) {
      return assertThatExceptionOfType(exceptionType).isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, T> after(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      return assertThatThrownBy(throwingCallable).asInstanceOf(throwable(exceptionType));
    }
  }

  static final class AssertThatThrownByHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType).hasMessage(message);
    }
  }

  static final class AssertThatThrownByRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .rootCause()
          .hasMessage(message);
    }
  }

  static final class AssertThatThrownByHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message,
        @Repeated Object parameters) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message,
        @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessage(message, parameters);
    }
  }

  static final class AssertThatThrownByHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageStartingWith(message);
    }
  }

  static final class AssertThatThrownByHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageContaining(message);
    }
  }

  static final class AssertThatThrownByHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    AbstractObjectAssert<?, ?> before(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        ThrowingCallable throwingCallable,
        Class<? extends Throwable> exceptionType,
        String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageNotContaining(message);
    }
  }

  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
  static final class AbstractThrowableAssertHasMessage {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(String.format(message, parameters));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(message, parameters);
    }
  }

  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
  static final class AbstractThrowableAssertWithFailMessage {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object args) {
      return abstractThrowableAssert.withFailMessage(String.format(message, args));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object args) {
      return abstractThrowableAssert.withFailMessage(message, args);
    }
  }

  // XXX: This rule changes the `Throwable` against which subsequent assertions are made.
  static final class AbstractThrowableAssertCauseIsSameAs {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated API will be rewritten. */)
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert, Throwable expected) {
      return throwableAssert.hasCauseReference(expected);
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert, Throwable expected) {
      return throwableAssert.cause().isSameAs(expected);
    }
  }
}
