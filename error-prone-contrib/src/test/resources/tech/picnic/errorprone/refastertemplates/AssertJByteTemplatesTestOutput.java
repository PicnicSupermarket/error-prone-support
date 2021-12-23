package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractByteAssert;

final class AssertJByteTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1));
  }

  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1));
  }

  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 0).isEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 0).isNotEqualTo((byte) 0);
  }

  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 0).isEqualTo((byte) 1);
  }
}
