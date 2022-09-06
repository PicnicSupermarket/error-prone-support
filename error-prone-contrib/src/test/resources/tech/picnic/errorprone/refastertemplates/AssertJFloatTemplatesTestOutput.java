package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractFloatAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJFloatTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0F).isCloseTo(1, offset(0F)),
        assertThat(0F).isCloseTo(Float.valueOf(1), offset(0F)));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0F).isEqualTo(1), assertThat(0F).isEqualTo(1));
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsEqualToExpected() {
    return assertThat(1F).isEqualTo(2F);
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0F).isNotEqualTo(1), assertThat(0F).isNotEqualTo(1));
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsNotEqualToExpected() {
    return assertThat(1F).isNotEqualTo(2F);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsZero() {
    return assertThat(0F).isEqualTo(0);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsNotZero() {
    return assertThat(0F).isNotEqualTo(0);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsOne() {
    return assertThat(0F).isEqualTo(1);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsLessThanExpected() {
    return assertThat(1F).isLessThan(2F);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsLessThanOrEqualToExpected() {
    return assertThat(1F).isLessThanOrEqualTo(2F);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsGreaterThanExpected() {
    return assertThat(1F).isGreaterThan(2F);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertActualIsGreaterThanOrEqualToExpected() {
    return assertThat(1F).isGreaterThanOrEqualTo(2F);
  }
}
