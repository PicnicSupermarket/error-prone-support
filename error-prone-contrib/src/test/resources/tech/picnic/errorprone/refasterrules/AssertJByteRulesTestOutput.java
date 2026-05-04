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
        assertThat((byte) 1).isEqualTo((byte) 2), assertThat((byte) 1).isEqualTo((byte) 2));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isNotEqualTo((byte) 2), assertThat((byte) 1).isNotEqualTo((byte) 2));
  }

  AbstractByteAssert<?> testAbstractByteAssertIsEqualToZero() {
    return assertThat((byte) 1).isEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotEqualToZero() {
    return assertThat((byte) 1).isNotEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsEqualToOne() {
    return assertThat((byte) 1).isEqualTo((byte) 1);
  }
}
