package tech.picnic.errorprone.refasterrules.input;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJThrowingCallableRulesTest implements RefasterRuleCollectionTestCase {
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessageParameters() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionHasMessageParameters() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageParameters() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageParameters() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownBy() {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageParameters() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo %s", "bar");
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertHasMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s %s", "bar", 1)));
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertWithFailMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s %s", "bar", 1)));
  }
}
