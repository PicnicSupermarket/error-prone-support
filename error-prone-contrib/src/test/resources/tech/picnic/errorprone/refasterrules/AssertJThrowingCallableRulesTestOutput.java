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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentException() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IllegalArgumentException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalArgumentExceptionRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .rootCause()
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByIllegalStateExceptionRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalStateException.class)
        .rootCause()
        .hasMessage("foo");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .rootCause()
        .hasMessage("foo");
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

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByNullPointerExceptionHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(NullPointerException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOException() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class);
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessage() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageParameters() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageContaining() {
    return assertThatThrownBy(() -> {}).isInstanceOf(IOException.class).hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByIOExceptionHasMessageNotContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IOException.class)
        .hasMessageNotContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByAsInstanceOfThrowable() {
    return assertThatThrownBy(() -> {}).asInstanceOf(throwable(IllegalArgumentException.class));
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByRootCauseHasMessage() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .rootCause()
        .hasMessage("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageParameters() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("foo %s", "bar");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageStartingWith() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageContaining() {
    return assertThatThrownBy(() -> {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foo");
  }

  AbstractObjectAssert<?, ?> testAssertThatThrownByHasMessageNotContaining() {
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
