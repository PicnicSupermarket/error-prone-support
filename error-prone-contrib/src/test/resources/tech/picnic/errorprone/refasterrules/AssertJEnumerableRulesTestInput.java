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
    assertThat(ImmutableSet.of(5)).size().isNotPositive().returnToIterable();
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeGreaterThan(0),
        assertThat(ImmutableSet.of(2)).hasSizeGreaterThanOrEqualTo(1),
        assertThat(ImmutableSet.of(3)).size().isNotEqualTo(0),
        assertThat(ImmutableSet.of(4)).size().isPositive(),
        assertThat(ImmutableSet.of(5)).size().isNotEqualTo(0).returnToIterable(),
        assertThat(ImmutableSet.of(6)).size().isPositive().returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSize() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isEqualTo(2),
        assertThat(ImmutableSet.of(3)).size().isEqualTo(4).returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isLessThan(2),
        assertThat(ImmutableSet.of(3)).size().isLessThan(4).returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isLessThanOrEqualTo(2),
        assertThat(ImmutableSet.of(3)).size().isLessThanOrEqualTo(4).returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isGreaterThan(2),
        assertThat(ImmutableSet.of(3)).size().isGreaterThan(4).returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isGreaterThanOrEqualTo(2),
        assertThat(ImmutableSet.of(3)).size().isGreaterThanOrEqualTo(4).returnToIterable());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeBetween() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isBetween(2, 3),
        assertThat(ImmutableSet.of(4)).size().isBetween(5, 6).returnToIterable());
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSize(Iterables.size(ImmutableSet.of(2))),
        assertThat(ImmutableSet.of(3)).hasSize(ImmutableSet.of(4).size()),
        assertThat(ImmutableSet.of(5)).hasSize(new Integer[0].length));
  }
}
