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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessage() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassRootCauseHasMessage() {
    return assertThatIllegalArgumentException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageVarargs() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageStartingWith() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageContaining() {
    return assertThatIllegalArgumentException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageNotContainingAny() {
    return assertThatIllegalArgumentException()
        .isThrownBy(() -> {})
        .withMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIllegalStateExceptionClass() {
    return assertThatIllegalStateException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessage() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassRootCauseHasMessage() {
    return assertThatIllegalStateException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageVarargs() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageStartingWith() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageNotContaining() {
    return assertThatIllegalStateException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfNullPointerExceptionClass() {
    return assertThatNullPointerException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessage() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassRootCauseHasMessage() {
    return assertThatNullPointerException()
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageVarargs() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageStartingWith() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageContaining() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageNotContaining() {
    return assertThatNullPointerException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClass() {
    return assertThatIOException().isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessage() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassRootCauseHasMessage() {
    return assertThatIOException().isThrownBy(() -> {}).havingRootCause().withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageVarargs() {
    return assertThatIOException().isThrownBy(() -> {}).withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageStartingWith() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageContaining() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageNotContaining() {
    return assertThatIOException().isThrownBy(() -> {}).withMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByAsInstanceOfThrowable() {
    return assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {});
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfRootCauseHasMessage() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .havingRootCause()
        .withMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageVarargs() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageStartingWith() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageContaining() {
    return assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> {})
        .withMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageNotContaining() {
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
