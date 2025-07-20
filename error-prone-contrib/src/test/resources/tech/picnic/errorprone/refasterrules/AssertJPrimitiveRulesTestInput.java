package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPrimitiveRulesTest implements RefasterRuleCollectionTestCase {
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

  void testAssertThatArrayIsEmpty() {
    assertThat(new String[0].length).isZero();
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSize() {
    return assertThat(new String[7].length).isEqualTo(7);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSameSizeAs() {
    return assertThat(new String[3].length).isEqualTo(new Integer[3].length);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThanOrEqualTo() {
    return assertThat(new String[2].length).isLessThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeLessThan() {
    return assertThat(new String[4].length).isLessThan(5);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThan() {
    return assertThat(new String[5].length).isGreaterThan(4);
  }

  AbstractAssert<?, ?> testAssertThatArrayHasSizeGreaterThanOrEqualTo() {
    return assertThat(new String[1].length).isGreaterThanOrEqualTo(1);
  }
}
