package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJThrowingCallableTemplatesTest implements RefasterRuleCollectionTestCase {
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
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessageParameters() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIllegalArgumentExceptionHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIllegalArgumentExceptionHasMessageNotContainingAny() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateException() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageParameters() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerException() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageParameters() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOException() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageParameters() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownBy() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageParameters() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo %s", "bar");
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertHasMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("foo %s", "bar"),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("foo %s %s", "bar", 1));
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertWithFailMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage("foo %s", "bar"),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage("foo %s %s", "bar", 1));
  }
}
