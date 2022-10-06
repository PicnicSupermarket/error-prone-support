package tech.picnic.errorprone.refastertemplates.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractByteAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJByteTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isCloseTo((byte) 1, offset((byte) 0)),
        assertThat((byte) 0).isCloseTo((byte) 1, withPercentage(0)));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotCloseTo((byte) 1, offset((byte) 0)),
        assertThat((byte) 0).isNotCloseTo((byte) 1, withPercentage(0)));
  }

  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 0).isZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 0).isNotZero();
  }

  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 0).isOne();
  }
}
