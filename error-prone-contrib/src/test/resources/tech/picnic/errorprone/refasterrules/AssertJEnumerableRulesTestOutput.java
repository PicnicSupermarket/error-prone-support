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
    assertThat(ImmutableSet.of(4)).isEmpty();
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
