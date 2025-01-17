package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJEnumerableRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  void testEnumerableAssertIsEmpty() {
    assertThat(ImmutableSet.of(1)).hasSize(0);
    assertThat(ImmutableSet.of(2)).hasSizeLessThanOrEqualTo(0);
    assertThat(ImmutableSet.of(3)).hasSizeLessThan(1);
    assertThat(ImmutableSet.of(4)).size().isNotPositive();
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeGreaterThan(0),
        assertThat(ImmutableSet.of(2)).hasSizeGreaterThanOrEqualTo(1),
        assertThat(ImmutableSet.of(3)).size().isNotEqualTo(0),
        assertThat(ImmutableSet.of(4)).size().isPositive());
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSize() {
    return assertThat(ImmutableSet.of(1)).size().isEqualTo(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeLessThan() {
    return assertThat(ImmutableSet.of(1)).size().isLessThan(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeLessThanOrEqualTo() {
    return assertThat(ImmutableSet.of(1)).size().isLessThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeGreaterThan() {
    return assertThat(ImmutableSet.of(1)).size().isGreaterThan(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeGreaterThanOrEqualTo() {
    return assertThat(ImmutableSet.of(1)).size().isGreaterThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeBetween() {
    return assertThat(ImmutableSet.of(1)).size().isBetween(2, 3);
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSize(Iterables.size(ImmutableSet.of(2))),
        assertThat(ImmutableSet.of(3)).hasSize(ImmutableSet.of(4).size()),
        assertThat(ImmutableSet.of(5)).hasSize(new Integer[0].length));
  }
}
