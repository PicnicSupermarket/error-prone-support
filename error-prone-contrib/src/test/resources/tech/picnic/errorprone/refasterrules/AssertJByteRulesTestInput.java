package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractByteAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJByteRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isCloseTo((byte) 2, offset((byte) 0)),
        assertThat((byte) 1).isCloseTo((byte) 2, withPercentage(0)));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isNotCloseTo((byte) 2, offset((byte) 0)),
        assertThat((byte) 1).isNotCloseTo((byte) 2, withPercentage(0)));
  }

  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 1).isZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 1).isNotZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 1).isOne();
  }
}
