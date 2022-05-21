package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;

final class AssertJThrowingCallableTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        assertThatExceptionOfType(Throwable.class),
        assertThatIOException(),
        assertThatIllegalArgumentException(),
        assertThatIllegalStateException(),
        assertThatNullPointerException());
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentException() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessage() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIllegalArgumentExceptionHasMessageStartingWith() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessageContaining() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIllegalArgumentExceptionHasMessageNotContainingAny() {
    return assertThatIllegalArgumentException()
        .isThrownBy(() -> {})
        .withMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateException() {
    return assertThatIllegalStateException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessage() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageStartingWith() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageNotContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerException() {
    return assertThatNullPointerException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessage() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageStartingWith() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOException() {
    return assertThatIOException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessage() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownBy() {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo");
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testThrowableAssertAlternativeHasMessageArgs() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s %f", "bar", 1)));
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testThrowableAssertAlternativeWithFailMessageArgs() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s %f", "bar", 1)));
  }
}
