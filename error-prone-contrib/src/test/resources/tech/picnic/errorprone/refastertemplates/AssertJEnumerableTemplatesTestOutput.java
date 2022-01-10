package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJEnumableTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  void testEnumerableAssertIsEmpty() {
    assertThat(ImmutableSet.of()).isEmpty();
    assertThat(ImmutableSet.of()).isEmpty();
    assertThat(ImmutableSet.of()).isEmpty();
  }

  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(assertThat("foo").isNotEmpty(), assertThat("bar").isNotEmpty());
  }

  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSameSizeAs(ImmutableSet.of(2)),
        assertThat(ImmutableSet.of(3)).hasSameSizeAs(ImmutableSet.of(4)),
        assertThat(ImmutableSet.of(5)).hasSameSizeAs(new Integer[0]));
  }
}
