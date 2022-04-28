package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.assertj.core.api.AbstractObjectAssert;

final class AssertJExceptionTemplatesTest implements RefasterTemplateTestCase {

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrown() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalArgumentExceptionIsThrownWithMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      assertThatIllegalArgumentExceptionIsThrownWithMessageNotContainingAny() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrown() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIllegalStateExceptionIsThrownWithMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrown() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class);
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrownWithMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> assertThatNullPointerExceptionIsThrownWithMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> assertThatIOExceptionIsThrown() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class);
  }

  AbstractObjectAssert<?, ?> assertThatExceptionOfTypeIsThrown() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> assertThatExceptionOfTypeIsThrownWithMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }
}
