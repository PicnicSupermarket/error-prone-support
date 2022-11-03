package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJEnumerableRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  void testEnumerableAssertIsEmpty() {
    assertThat(ImmutableSet.of()).hasSize(0);
    assertThat(ImmutableSet.of()).hasSizeLessThanOrEqualTo(0);
    assertThat(ImmutableSet.of()).hasSizeLessThan(1);
  }

  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo").hasSizeGreaterThan(0), assertThat("bar").hasSizeGreaterThanOrEqualTo(1));
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSize(Iterables.size(ImmutableSet.of(2))),
        assertThat(ImmutableSet.of(3)).hasSize(ImmutableSet.of(4).size()),
        assertThat(ImmutableSet.of(5)).hasSize(new Integer[0].length));
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertMapHasSameSizeAs() {
    return ImmutableSet.of(assertThat(ImmutableSet.of(1)).hasSize(ImmutableMap.of(2, 3).size()));
  }
}
