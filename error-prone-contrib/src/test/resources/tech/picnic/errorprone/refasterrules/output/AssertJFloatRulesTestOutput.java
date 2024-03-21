package tech.picnic.errorprone.refasterrules.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractFloatAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJFloatRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
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

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0F).isNotEqualTo(1), assertThat(0F).isNotEqualTo(1));
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
}
