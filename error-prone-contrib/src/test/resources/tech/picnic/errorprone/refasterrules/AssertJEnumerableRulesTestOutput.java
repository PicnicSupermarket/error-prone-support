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
    assertThat(ImmutableSet.of(1)).isEmpty();
    assertThat(ImmutableSet.of(2)).isEmpty();
    assertThat(ImmutableSet.of(3)).isEmpty();
    assertThat(ImmutableSet.of(4)).hasSameSizeAs(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(5)).isEmpty();
    assertThat(ImmutableSet.of(6)).containsExactlyElementsOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(7)).isEmpty();
    assertThat(ImmutableSet.of(8)).containsExactlyInAnyOrderElementsOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(9)).isEmpty();
    assertThat(ImmutableSet.of(10)).hasSameElementsAs(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(11)).isEmpty();
    assertThat(ImmutableSet.of(12)).isSubsetOf(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(13)).isEmpty();
    assertThat(ImmutableSet.of(14)).isEmpty();
    assertThat(ImmutableSet.of(15)).isEmpty();
    assertThat(ImmutableSet.of(16)).isEmpty();
    assertThat(ImmutableSet.of(17)).isEmpty();
    assertThat(ImmutableSet.of(18)).isEmpty();
  }

  void testAssertIsEmpty() {
    assertThat(ImmutableSet.of(1)).isEqualTo(ImmutableSet.of(0));
    assertThat(ImmutableSet.of(2)).isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).isNotEmpty(),
        assertThat(ImmutableSet.of(2)).isNotEmpty(),
        assertThat(ImmutableSet.of(3)).isNotEmpty(),
        assertThat(ImmutableSet.of(4)).isNotEmpty(),
        assertThat(ImmutableSet.of(5)).isNotEmpty(),
        assertThat(ImmutableSet.of(6)).isNotEmpty());
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSize() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSize(2), assertThat(ImmutableSet.of(3)).hasSize(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeLessThan(2),
        assertThat(ImmutableSet.of(3)).hasSizeLessThan(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeLessThanOrEqualTo(2),
        assertThat(ImmutableSet.of(3)).hasSizeLessThanOrEqualTo(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThan() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeGreaterThan(2),
        assertThat(ImmutableSet.of(3)).hasSizeGreaterThan(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeGreaterThanOrEqualTo(2),
        assertThat(ImmutableSet.of(3)).hasSizeGreaterThanOrEqualTo(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testEnumerableAssertHasSizeBetween() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSizeBetween(2, 3),
        assertThat(ImmutableSet.of(4)).hasSizeBetween(5, 6));
  }

  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat("foo").hasSameSizeAs(ImmutableSet.of(1)),
        assertThat("bar").hasSameSizeAs(ImmutableSet.of(2)),
        assertThat("baz").hasSameSizeAs(new Integer[0]),
        assertThat("qux").hasSameSizeAs("quux"));
  }
}
