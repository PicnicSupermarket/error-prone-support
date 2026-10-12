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
import java.io.IOException;
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
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageVarargs() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageNotContainingAny() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContainingAny("foo", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIllegalStateExceptionClass() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class);
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalStateException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageVarargs() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfNullPointerExceptionClass() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class);
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(NullPointerException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageVarargs() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClass() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageVarargs() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageContaining() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?>
      testAssertThatThrownByIsInstanceOfIOExceptionClassHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByAsInstanceOfThrowable() {
    return assertThatThrownBy(() -> {}).asInstanceOf(throwable(IllegalArgumentException.class));
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageVarargs() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIsInstanceOfHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining("foo");
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

  @SuppressWarnings("deprecation" /* Rule targets deprecated API. */)
  AbstractThrowableAssert<?, ? extends Throwable> testAbstractThrowableAssertCauseIsSameAs() {
    return assertThat(new IllegalStateException()).cause().isSameAs(new IllegalArgumentException());
  }
}
