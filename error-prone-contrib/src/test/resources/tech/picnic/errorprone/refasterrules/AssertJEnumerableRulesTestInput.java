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
    assertThat(ImmutableSet.of(4)).hasSameSizeAs(ImmutableSet.of());
    assertThat(ImmutableSet.of(5)).hasSameSizeAs(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(6)).containsExactlyElementsOf(ImmutableSet.of());
    assertThat(ImmutableSet.of(7)).containsExactlyElementsOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(8)).containsExactlyInAnyOrderElementsOf(ImmutableSet.of());
    assertThat(ImmutableSet.of(9)).containsExactlyInAnyOrderElementsOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(10)).hasSameElementsAs(ImmutableSet.of());
    assertThat(ImmutableSet.of(11)).hasSameElementsAs(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(12)).isSubsetOf(ImmutableSet.of());
    assertThat(ImmutableSet.of(13)).isSubsetOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(14)).containsExactly();
    assertThat(ImmutableSet.of(15)).containsExactlyInAnyOrder();
    assertThat(ImmutableSet.of(16)).containsOnly();
    assertThat(ImmutableSet.of(17)).isSubsetOf();
    assertThat(ImmutableSet.of(18)).size().isNotPositive();
  }

  void testAssertAndEnumerableAssertIsEmpty() {
    assertThat(ImmutableSet.of(1)).isEqualTo(ImmutableSet.of());
    assertThat(ImmutableSet.of(2)).isEqualTo(ImmutableSet.of(0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeGreaterThan(0),
        assertThat(ImmutableSet.of(2)).hasSizeGreaterThanOrEqualTo(1),
        assertThat(ImmutableSet.of(3)).size().isNotEqualTo(0).returnToIterable(),
        assertThat(ImmutableSet.of(4)).size().isPositive().returnToIterable(),
        assertThat(ImmutableSet.of(5)).size().isNotEqualTo(0),
        assertThat(ImmutableSet.of(6)).size().isPositive());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSize() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isEqualTo(2).returnToIterable(),
        assertThat(ImmutableSet.of(3)).size().isEqualTo(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isLessThan(2).returnToIterable(),
        assertThat(ImmutableSet.of(3)).size().isLessThan(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isLessThanOrEqualTo(2).returnToIterable(),
        assertThat(ImmutableSet.of(3)).size().isLessThanOrEqualTo(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isGreaterThan(2).returnToIterable(),
        assertThat(ImmutableSet.of(3)).size().isGreaterThan(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isGreaterThanOrEqualTo(2).returnToIterable(),
        assertThat(ImmutableSet.of(3)).size().isGreaterThanOrEqualTo(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeBetween() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size().isBetween(2, 3).returnToIterable(),
        assertThat(ImmutableSet.of(4)).size().isBetween(5, 6));
  }

  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat("foo").hasSize(Iterables.size(ImmutableSet.of(1))),
        assertThat("bar").hasSize(ImmutableSet.of(2).size()),
        assertThat("baz").hasSize(new Integer[0].length),
        assertThat("qux").hasSize("quux".length()));
  }
}
