package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractShortAssert;

final class AssertJShortTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isCloseTo((short) 1, offset((short) 0)),
        assertThat((short) 0).isCloseTo((short) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractShortAssert<?>> testAbstractShortAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((short) 0).isNotCloseTo((short) 1, offset((short) 0)),
        assertThat((short) 0).isNotCloseTo((short) 1, withPercentage(0)));
  }

  AbstractShortAssert<?> testAbstractShortAssertIsZero() {
    return assertThat((short) 0).isZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsNotZero() {
    return assertThat((short) 0).isNotZero();
  }

  AbstractShortAssert<?> testAbstractShortAssertIsOne() {
    return assertThat((short) 0).isOne();
  }
}
