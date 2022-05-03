package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.assertj.core.api.AbstractObjectAssert;

final class AssertJExceptionTemplatesTest implements RefasterTemplateTestCase {
  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrown() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessage() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessageStartingWith() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessageContaining() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      assertThatIllegalArgumentExceptionIsThrownWithMessageNotContainingAny() {
    return assertThatIllegalArgumentException()
        .isThrownBy(() -> {})
        .withMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrown() {
    return assertThatIllegalStateException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessage() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageStartingWith() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageNotContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrown() {
    return assertThatNullPointerException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrownWithMessage() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrownWithMessageStartingWith() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIOExceptionIsThrown() {
    return assertThatIOException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> assertThatExceptionOfTypeIsThrown() {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> assertThatExceptionOfTypeIsThrownWithMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo");
  }
}
