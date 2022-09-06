package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJShortTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isEqualTo((short) 1), assertThat((short) 0).isEqualTo((short) 1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isEqualTo((short) 2), assertThat((short) 1).isEqualTo((short) 2));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isNotEqualTo((short) 1),
        assertThat((short) 0).isNotEqualTo((short) 1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat((short) 1).isNotEqualTo((short) 2));
  }

  AbstractShortAssert<?> testAbstractShortAssertIsZero() {
    return assertThat((short) 0).isEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotZero() {
    return assertThat((short) 0).isNotEqualTo((short) 0);
  }

  AbstractShortAssert<?> testAbstractShortAssertIsOne() {
    return assertThat((short) 0).isEqualTo((short) 1);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isLessThan((short) 2), assertThat((short) 1).isLessThan((short) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isLessThanOrEqualTo((short) 2),
        assertThat((short) 1).isLessThanOrEqualTo((short) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isGreaterThan((short) 2),
        assertThat((short) 1).isGreaterThan((short) 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractShortAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2));
  }
}
