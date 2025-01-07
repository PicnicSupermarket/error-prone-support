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
        assertThat(ImmutableSet.of(4)).isNotEmpty());
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSize() {
    return assertThat(ImmutableSet.of(1)).hasSize(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeLessThan() {
    return assertThat(ImmutableSet.of(1)).hasSizeLessThan(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeLessThanOrEqualTo() {
    return assertThat(ImmutableSet.of(1)).hasSizeLessThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeGreaterThan() {
    return assertThat(ImmutableSet.of(1)).hasSizeGreaterThan(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeGreaterThanOrEqualTo() {
    return assertThat(ImmutableSet.of(1)).hasSizeGreaterThanOrEqualTo(2);
  }

  AbstractAssert<?, ?> testEnumerableAssertHasSizeBetween() {
    return assertThat(ImmutableSet.of(1)).hasSizeBetween(2, 3);
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSameSizeAs(ImmutableSet.of(2)),
        assertThat(ImmutableSet.of(3)).hasSameSizeAs(ImmutableSet.of(4)),
        assertThat(ImmutableSet.of(5)).hasSameSizeAs(new Integer[0]));
  }
}
