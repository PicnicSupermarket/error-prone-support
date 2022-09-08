package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJDoubleTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0.0).isEqualTo(1, offset(0.0)),
        assertThat(0.0).isEqualTo(Double.valueOf(1), offset(0.0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isCloseTo(1, offset(0.0)), assertThat(0.0).isCloseTo(1, withPercentage(0)));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsEqualTo() {
    return ImmutableSet.of(
        assertThat(true == false).isTrue(),
        assertThat(true != false).isFalse(),
        assertThat((byte) 1 == (byte) 2).isTrue(),
        assertThat((byte) 1 != (byte) 2).isFalse(),
        assertThat((char) 1 == (char) 2).isTrue(),
        assertThat((char) 1 != (char) 2).isFalse(),
        assertThat((short) 1 == (short) 2).isTrue(),
        assertThat((short) 1 != (short) 2).isFalse(),
        assertThat(1 == 2).isTrue(),
        assertThat(1 != 2).isFalse(),
        assertThat(1L == 2L).isTrue(),
        assertThat(1L != 2L).isFalse(),
        assertThat(1F == 2F).isTrue(),
        assertThat(1F != 2F).isFalse(),
        assertThat(1.0 == 2.0).isTrue(),
        assertThat(1.0 != 2.0).isFalse());
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isNotCloseTo(1, offset(0.0)),
        assertThat(0.0).isNotCloseTo(1, withPercentage(0)));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(true != false).isTrue(),
        assertThat(true == false).isFalse(),
        assertThat((byte) 1 != (byte) 2).isTrue(),
        assertThat((byte) 1 == (byte) 2).isFalse(),
        assertThat((char) 1 != (char) 2).isTrue(),
        assertThat((char) 1 == (char) 2).isFalse(),
        assertThat((short) 1 != (short) 2).isTrue(),
        assertThat((short) 1 == (short) 2).isFalse(),
        assertThat(1 != 2).isTrue(),
        assertThat(1 == 2).isFalse(),
        assertThat(1L != 2L).isTrue(),
        assertThat(1L == 2L).isFalse(),
        assertThat(1F != 2F).isTrue(),
        assertThat(1F == 2F).isFalse(),
        assertThat(1.0 != 2.0).isTrue(),
        assertThat(1.0 == 2.0).isFalse());
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsZero() {
    return assertThat(0.0).isZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotZero() {
    return assertThat(0.0).isNotZero();
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsOne() {
    return assertThat(0.0).isOne();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThan() {
    return ImmutableSet.of(
        assertThat((byte) 1 < (byte) 2).isTrue(),
        assertThat((byte) 1 >= (byte) 2).isFalse(),
        assertThat((char) 1 < (char) 2).isTrue(),
        assertThat((char) 1 >= (char) 2).isFalse(),
        assertThat((short) 1 < (short) 2).isTrue(),
        assertThat((short) 1 >= (short) 2).isFalse(),
        assertThat(1 < 2).isTrue(),
        assertThat(1 >= 2).isFalse(),
        assertThat(1L < 2L).isTrue(),
        assertThat(1L >= 2L).isFalse(),
        assertThat(1F < 2F).isTrue(),
        assertThat(1F >= 2F).isFalse(),
        assertThat(1.0 < 2.0).isTrue(),
        assertThat(1.0 >= 2.0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1 <= (byte) 2).isTrue(),
        assertThat((byte) 1 > (byte) 2).isFalse(),
        assertThat((char) 1 <= (char) 2).isTrue(),
        assertThat((char) 1 > (char) 2).isFalse(),
        assertThat((short) 1 <= (short) 2).isTrue(),
        assertThat((short) 1 > (short) 2).isFalse(),
        assertThat(1 <= 2).isTrue(),
        assertThat(1 > 2).isFalse(),
        assertThat(1L <= 2L).isTrue(),
        assertThat(1L > 2L).isFalse(),
        assertThat(1F <= 2F).isTrue(),
        assertThat(1F > 2F).isFalse(),
        assertThat(1.0 <= 2.0).isTrue(),
        assertThat(1.0 > 2.0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThan() {
    return ImmutableSet.of(
        assertThat((byte) 1 > (byte) 2).isTrue(),
        assertThat((byte) 1 <= (byte) 2).isFalse(),
        assertThat((char) 1 > (char) 2).isTrue(),
        assertThat((char) 1 <= (char) 2).isFalse(),
        assertThat((short) 1 > (short) 2).isTrue(),
        assertThat((short) 1 <= (short) 2).isFalse(),
        assertThat(1 > 2).isTrue(),
        assertThat(1 <= 2).isFalse(),
        assertThat(1L > 2L).isTrue(),
        assertThat(1L <= 2L).isFalse(),
        assertThat(1F > 2F).isTrue(),
        assertThat(1F <= 2F).isFalse(),
        assertThat(1.0 > 2.0).isTrue(),
        assertThat(1.0 <= 2.0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1 >= (byte) 2).isTrue(),
        assertThat((byte) 1 < (byte) 2).isFalse(),
        assertThat((char) 1 >= (char) 2).isTrue(),
        assertThat((char) 1 < (char) 2).isFalse(),
        assertThat((short) 1 >= (short) 2).isTrue(),
        assertThat((short) 1 < (short) 2).isFalse(),
        assertThat(1 >= 2).isTrue(),
        assertThat(1 < 2).isFalse(),
        assertThat(1L >= 2L).isTrue(),
        assertThat(1L < 2L).isFalse(),
        assertThat(1F >= 2F).isTrue(),
        assertThat(1F < 2F).isFalse(),
        assertThat(1.0 >= 2.0).isTrue(),
        assertThat(1.0 < 2.0).isFalse());
  }
}
