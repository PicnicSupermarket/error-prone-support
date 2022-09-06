package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJLongTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsEqualTo() {
    return ImmutableSet.of(assertThat(0L).isEqualTo(1), assertThat(0L).isEqualTo(1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsEqualToExpected() {
    return ImmutableSet.of(assertThat(1L).isEqualTo(2L), assertThat(1L).isEqualTo(2L));
  }

  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsNotEqualTo() {
    return ImmutableSet.of(assertThat(0L).isNotEqualTo(1), assertThat(0L).isNotEqualTo(1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsNotEqualToExpected() {
    return ImmutableSet.of(assertThat(1L).isNotEqualTo(2L), assertThat(1L).isNotEqualTo(2L));
  }

  AbstractLongAssert<?> testAbstractLongAssertIsZero() {
    return assertThat(0L).isEqualTo(0);
  }

  AbstractLongAssert<?> testAbstractLongAssertIsNotZero() {
    return assertThat(0L).isNotEqualTo(0);
  }

  AbstractLongAssert<?> testAbstractLongAssertIsOne() {
    return assertThat(0L).isEqualTo(1);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsLessThanExpected() {
    return ImmutableSet.of(assertThat(1L).isLessThan(2L), assertThat(1L).isLessThan(2L));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1L).isLessThanOrEqualTo(2L), assertThat(1L).isLessThanOrEqualTo(2L));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(assertThat(1L).isGreaterThan(2L), assertThat(1L).isGreaterThan(2L));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractLongAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(1L).isGreaterThanOrEqualTo(2L), assertThat(1L).isGreaterThanOrEqualTo(2L));
  }
}
