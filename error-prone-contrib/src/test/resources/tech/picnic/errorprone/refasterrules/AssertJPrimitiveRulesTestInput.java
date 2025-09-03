package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPrimitiveRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isSameAs(false),
        assertThat(true).isSameAs(false),
        assertThat((byte) 1).isSameAs((byte) 2),
        assertThat((byte) 1).isSameAs((byte) 2),
        assertThat((char) 1).isSameAs((char) 2),
        assertThat((char) 1).isSameAs((char) 2),
        assertThat((short) 1).isSameAs((short) 2),
        assertThat((short) 1).isSameAs((short) 2),
        assertThat(1).isSameAs(2),
        assertThat(1).isSameAs(2),
        assertThat(1L).isSameAs(2L),
        assertThat(1L).isSameAs(2L),
        assertThat(1F).isSameAs(2F),
        assertThat(1F).isSameAs(2F),
        assertThat(1.0).isSameAs(2.0),
        assertThat(1.0).isSameAs(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isNotSameAs(false),
        assertThat(true).isNotSameAs(false),
        assertThat((byte) 1).isNotSameAs((byte) 2),
        assertThat((byte) 1).isNotSameAs((byte) 2),
        assertThat((char) 1).isNotSameAs((char) 2),
        assertThat((char) 1).isNotSameAs((char) 2),
        assertThat((short) 1).isNotSameAs((short) 2),
        assertThat((short) 1).isNotSameAs((short) 2),
        assertThat(1).isNotSameAs(2),
        assertThat(1).isNotSameAs(2),
        assertThat(1L).isNotSameAs(2L),
        assertThat(1L).isNotSameAs(2L),
        assertThat(1F).isNotSameAs(2F),
        assertThat(1F).isNotSameAs(2F),
        assertThat(1.0).isNotSameAs(2.0),
        assertThat(1.0).isNotSameAs(2.0));
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
