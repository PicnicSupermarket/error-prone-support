package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.io.IOException;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/**
 * Refaster templates related to AssertJ assertions over methods producing {@link Throwable}s.
 *
 * <p>To unify the way we assert throwables in unit tests, we have chosen to use the form {@link
 * org.assertj.core.api.Assertions#assertThatThrownBy}. Note that many more combinations can be made
 * with exception-specific assertions than covered here. The templates below replace those
 * combinations that were encountered the most.
 */
final class AssertJExceptionTemplates {
  private AssertJExceptionTemplates() {}

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

  static final class AssertThatThrownByIllegalArgumentExceptionWithMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalArgumentExceptionWithMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalArgumentExceptionWithMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalArgumentExceptionWithMessageNotContainingAny {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalStateExceptionWithMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalStateExceptionWithMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalStateExceptionWithMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByIllegalStateExceptionWithMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByNPE {
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

  static final class AssertThatThrownByNPEWithMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByNPE" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownByNPEWithMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByNPE" /* Matches strictly more specific expressions. */)
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

  static final class AssertThatThrownBy {
    @BeforeTemplate
    AbstractObjectAssert<?, ?> before(
        Class<? extends Throwable> exceptionType, ThrowingCallable throwingCallable) {
      return assertThatExceptionOfType(exceptionType).isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        Class<? extends Throwable> exceptionType, ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType);
    }
  }

  static final class AssertThatThrownByWithMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownBy" /* Matches strictly more specific expressions. */)
    AbstractObjectAssert<?, ?> before(
        Class<? extends Throwable> exceptionType,
        ThrowingCallable throwingCallable,
        String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, ?> after(
        Class<? extends Throwable> exceptionType,
        ThrowingCallable throwingCallable,
        String message) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType).hasMessage(message);
    }
  }
}
