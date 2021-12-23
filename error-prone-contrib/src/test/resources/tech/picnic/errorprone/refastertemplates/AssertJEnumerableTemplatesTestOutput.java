package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertHasSameSizeAs;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertIsNotEmpty;

@TemplateCollection(AssertJEnumerableTemplates.class)
final class AssertJEnumerableTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty1() {
    assertThat(ImmutableSet.of()).isEmpty();
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty2() {
    assertThat(ImmutableSet.of()).isEmpty();
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty3() {
    assertThat(ImmutableSet.of()).isEmpty();
  }

  @Template(EnumerableAssertIsNotEmpty.class)
  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(assertThat("foo").isNotEmpty(), assertThat("bar").isNotEmpty());
  }

  @Template(EnumerableAssertHasSameSizeAs.class)
  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSameSizeAs(ImmutableSet.of(2)),
        assertThat(ImmutableSet.of(3)).hasSameSizeAs(ImmutableSet.of(4)),
        assertThat(ImmutableSet.of(5)).hasSameSizeAs(new Integer[0]));
  }
}
