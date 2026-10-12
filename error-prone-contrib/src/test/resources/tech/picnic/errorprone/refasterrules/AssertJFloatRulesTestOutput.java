package tech.picnic.errorprone.refasterrules;

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

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsCloseTo() {
    return ImmutableSet.of(
        assertThat(1f).isCloseTo(2, offset(0f)),
        assertThat(1f).isCloseTo(Float.valueOf(2), offset(0f)));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(1f).isEqualTo(2), assertThat(1f).isEqualTo(2));
  }

  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(1f).isNotEqualTo(2), assertThat(1f).isNotEqualTo(2));
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsEqualToZero() {
    return assertThat(1f).isEqualTo(0);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsNotEqualToZero() {
    return assertThat(1f).isNotEqualTo(0);
  }

  AbstractFloatAssert<?> testAbstractFloatAssertIsEqualToOne() {
    return assertThat(1f).isEqualTo(1);
  }
}
