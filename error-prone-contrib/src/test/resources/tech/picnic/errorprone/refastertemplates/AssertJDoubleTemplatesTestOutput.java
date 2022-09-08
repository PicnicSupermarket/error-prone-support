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
        assertThat(0.0).isCloseTo(1, offset(0.0)),
        assertThat(0.0).isCloseTo(Double.valueOf(1), offset(0.0)));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0.0).isEqualTo(1), assertThat(0.0).isEqualTo(1));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(false),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat(1).isEqualTo(2),
        assertThat(1).isEqualTo(2),
        assertThat(1L).isEqualTo(2L),
        assertThat(1L).isEqualTo(2L),
        assertThat(1F).isEqualTo(2F),
        assertThat(1F).isEqualTo(2F),
        assertThat(1.0).isEqualTo(2.0),
        assertThat(1.0).isEqualTo(2.0));
  }

  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0.0).isNotEqualTo(1), assertThat(0.0).isNotEqualTo(1));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(false),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1F).isNotEqualTo(2F),
        assertThat(1F).isNotEqualTo(2F),
        assertThat(1.0).isNotEqualTo(2.0),
        assertThat(1.0).isNotEqualTo(2.0));
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsZero() {
    return assertThat(0.0).isEqualTo(0);
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotZero() {
    return assertThat(0.0).isNotEqualTo(0);
  }

  AbstractDoubleAssert<?> testAbstractDoubleAssertIsOne() {
    return assertThat(0.0).isEqualTo(1);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isLessThan(2.0),
        assertThat(1.0).isLessThan(2.0),
        assertThat(((byte) 1)).isLessThan(((byte) 2)),
        assertThat(1F).isLessThan(2F),
        assertThat(1).isLessThan(2),
        assertThat(1L).isLessThan(2L),
        assertThat(((short) 1)).isLessThan(((short) 2)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isLessThanOrEqualTo(2.0),
        assertThat(1.0).isLessThanOrEqualTo(2.0),
        assertThat(((byte) 1)).isLessThanOrEqualTo(((byte) 2)),
        assertThat(1F).isLessThanOrEqualTo(2F),
        assertThat(1).isLessThanOrEqualTo(2),
        assertThat(1L).isLessThanOrEqualTo(2L),
        assertThat(((short) 1)).isLessThanOrEqualTo(((short) 2)));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractDoubleAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isGreaterThan(2.0),
        assertThat(1.0).isGreaterThan(2.0),
        assertThat(((byte) 1)).isGreaterThan(((byte) 2)),
        assertThat(1F).isGreaterThan(2F),
        assertThat(1).isGreaterThan(2),
        assertThat(1L).isGreaterThan(2L),
        assertThat(((short) 1)).isGreaterThan(((short) 2)));
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractDoubleAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1.0).isGreaterThanOrEqualTo(2.0),
        assertThat(1.0).isGreaterThanOrEqualTo(2.0),
        assertThat(((byte) 1)).isGreaterThanOrEqualTo(((byte) 2)),
        assertThat(1F).isGreaterThanOrEqualTo(2F),
        assertThat(1).isGreaterThanOrEqualTo(2),
        assertThat(1L).isGreaterThanOrEqualTo(2L),
        assertThat(((short) 1)).isGreaterThanOrEqualTo(((short) 2)));
  }
}
