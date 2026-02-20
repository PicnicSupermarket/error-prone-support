package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPrimitiveRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(false),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((char) 1).isEqualTo((char) 2),
        assertThat((char) 1).isEqualTo((char) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat(1).isEqualTo(2),
        assertThat(1).isEqualTo(2),
        assertThat(1L).isEqualTo(2L),
        assertThat(1L).isEqualTo(2L),
        assertThat(1f).isEqualTo(2f),
        assertThat(1f).isEqualTo(2f),
        assertThat(1.0).isEqualTo(2.0),
        assertThat(1.0).isEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(false),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((char) 1).isNotEqualTo((char) 2),
        assertThat((char) 1).isNotEqualTo((char) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1f).isNotEqualTo(2f),
        assertThat(1f).isNotEqualTo(2f),
        assertThat(1.0).isNotEqualTo(2.0),
        assertThat(1.0).isNotEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThan() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThan((byte) 2),
        assertThat((byte) 1).isLessThan((byte) 2),
        assertThat((char) 1).isLessThan((char) 2),
        assertThat((char) 1).isLessThan((char) 2),
        assertThat((short) 1).isLessThan((short) 2),
        assertThat((short) 1).isLessThan((short) 2),
        assertThat(1).isLessThan(2),
        assertThat(1).isLessThan(2),
        assertThat(1L).isLessThan(2L),
        assertThat(1L).isLessThan(2L),
        assertThat(1f).isLessThan(2f),
        assertThat(1f).isLessThan(2f),
        assertThat(1.0).isLessThan(2.0),
        assertThat(1.0).isLessThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2),
        assertThat((char) 1).isLessThanOrEqualTo((char) 2),
        assertThat((char) 1).isLessThanOrEqualTo((char) 2),
        assertThat((short) 1).isLessThanOrEqualTo((short) 2),
        assertThat((short) 1).isLessThanOrEqualTo((short) 2),
        assertThat(1).isLessThanOrEqualTo(2),
        assertThat(1).isLessThanOrEqualTo(2),
        assertThat(1L).isLessThanOrEqualTo(2L),
        assertThat(1L).isLessThanOrEqualTo(2L),
        assertThat(1f).isLessThanOrEqualTo(2f),
        assertThat(1f).isLessThanOrEqualTo(2f),
        assertThat(1.0).isLessThanOrEqualTo(2.0),
        assertThat(1.0).isLessThanOrEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThan() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThan((byte) 2),
        assertThat((byte) 1).isGreaterThan((byte) 2),
        assertThat((char) 1).isGreaterThan((char) 2),
        assertThat((char) 1).isGreaterThan((char) 2),
        assertThat((short) 1).isGreaterThan((short) 2),
        assertThat((short) 1).isGreaterThan((short) 2),
        assertThat(1).isGreaterThan(2),
        assertThat(1).isGreaterThan(2),
        assertThat(1L).isGreaterThan(2L),
        assertThat(1L).isGreaterThan(2L),
        assertThat(1f).isGreaterThan(2f),
        assertThat(1f).isGreaterThan(2f),
        assertThat(1.0).isGreaterThan(2.0),
        assertThat(1.0).isGreaterThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2),
        assertThat((char) 1).isGreaterThanOrEqualTo((char) 2),
        assertThat((char) 1).isGreaterThanOrEqualTo((char) 2),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2),
        assertThat(1).isGreaterThanOrEqualTo(2),
        assertThat(1).isGreaterThanOrEqualTo(2),
        assertThat(1L).isGreaterThanOrEqualTo(2L),
        assertThat(1L).isGreaterThanOrEqualTo(2L),
        assertThat(1f).isGreaterThanOrEqualTo(2f),
        assertThat(1f).isGreaterThanOrEqualTo(2f),
        assertThat(1.0).isGreaterThanOrEqualTo(2.0),
        assertThat(1.0).isGreaterThanOrEqualTo(2.0));
  }
}
