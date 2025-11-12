package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJThrowingCallableRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        assertThatExceptionOfType(Throwable.class),
        assertThatIOException(),
        assertThatIllegalArgumentException(),
        assertThatIllegalStateException(),
        assertThatNullPointerException(),
        type(Throwable.class));
  }

  void testAssertThatThrownByIsInstanceOf() {
    assertThatThrownBy(() -> {}).asInstanceOf(throwable(IllegalArgumentException.class));
    assertThatThrownBy(() -> {}).asInstanceOf(type(IllegalArgumentException.class));
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentException() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessage() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionRootCauseHasMessage() {
    return assertThatIllegalArgumentException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionRootCauseHasMessage() {
    return assertThatIllegalStateException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionRootCauseHasMessage() {
    return assertThatNullPointerException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageParameters() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageStartingWith() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageContaining() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageNotContaining() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOException() {
    return assertThatIOException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessage() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionRootCauseHasMessage() {
    return assertThatIOException().isThrownBy(() -> {}).havingRootCause().withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageParameters() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageStartingWith() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageContaining() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageNotContaining() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByAsInstanceOfThrowable() {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByRootCauseHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageParameters() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageStartingWith() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageContaining() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageNotContaining() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessageNotContaining("foo");
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertHasMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("foo %s".formatted("bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("foo %s %s".formatted("bar", 1)));
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testAbstractThrowableAssertWithFailMessage() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage("foo %s".formatted("bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage("foo %s %s".formatted("bar", 1)));
  }

  @SuppressWarnings("deprecation" /* Rule targets deprecated API. */)
  AbstractThrowableAssert<?, ? extends Throwable> testAbstractThrowableAssertCauseIsSameAs() {
    return assertThat(new IllegalStateException())
        .hasCauseReference(new IllegalArgumentException());
  }
}
