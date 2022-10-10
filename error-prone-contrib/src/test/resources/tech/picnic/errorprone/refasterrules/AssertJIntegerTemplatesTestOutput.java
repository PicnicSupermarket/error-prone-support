package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0).isEqualTo(1), assertThat(0).isEqualTo(1));
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0).isNotEqualTo(1), assertThat(0).isNotEqualTo(1));
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(0).isEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(0).isNotEqualTo(0);
  }

  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(0).isEqualTo(1);
  }
}
